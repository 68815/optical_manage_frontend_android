# 代码架构规范

## 核心原则

### 单一职责原则 (SRP)
- 每个类应该只有一个引起它变化的原因
- 一个类只负责一项职责，不要把不相关的功能混在一起

### 文件大小限制
- 单个 Java/Kotlin 文件建议不超过 **300 行代码**
- 如果超过 400 行，考虑拆分
- 超过 500 行是**严重警告**，考虑立即重构

---

## Android 架构分层规范

### 1. UI 层
- **Fragment/Activity**: 只负责视图绑定和生命周期管理
- **Adapter**: 列表适配器独立文件
- **ViewHolder**: 列表项视图持有者独立文件
- **Dialog**: 对话框逻辑独立封装

### 2. 业务逻辑层
- **ViewModel**: 管理UI状态和业务逻辑
- **UseCase/Interactor**: 复杂业务逻辑封装
- **Repository**: 数据仓库，统一数据访问接口

### 3. 数据层
- **Model**: 数据模型定义
- **DataSource**: 数据源抽象
- **ApiService**: 网络接口定义

---

## 常见反模式及解决方案

### 反模式：巨型 Fragment
**问题**: Fragment 包含了地图管理、定位、对话框、绘制逻辑等所有功能

**解决方案**: 按职责拆分
```
MapFragment (主控制器，只负责协调)
├── MapController (地图操作封装)
├── LocationHelper (定位功能封装)
├── ResourcePointManager (资源点业务逻辑)
├── CableDrawHelper (光缆绘制逻辑封装)
├── dialogs/
│   ├── AddResourcePointDialog (添加资源点对话框)
│   ├── EditResourcePointDialog (编辑资源点对话框)
│   └── CableSegmentDialog (光缆段对话框)
└── adapters/
    └── ResourcePointInfoWindowAdapter (信息窗口适配器)
```

### 反模式：接口实现堆砌
**问题**: 一个类实现了多个不相关的接口
```java
public class MapFragment extends Fragment implements 
    LocationSource, AMapLocationListener, OnMapClickListener, 
    OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener {
    // 800+ 行代码
}
```

**解决方案**: 使用委托模式
```java
public class MapFragment extends Fragment {
    private LocationDelegate locationDelegate;
    private MapInteractionDelegate mapDelegate;
    // 各司其职
}
```

---

## 具体拆分建议

### 对话框拆分
- 每个对话框独立一个类，继承 `DialogFragment` 或使用 Builder 模式
- 对话框的回调通过接口传递给调用者

### 监听器拆分
- 将监听器逻辑封装到独立的 Helper/Delegate 类中
- 通过回调接口与主类通信

### 工具方法拆分
- 通用工具方法放入 `utils` 包
- 业务相关工具方法放入对应的 `helper` 或 `manager` 类

---

## 命名规范

### 类命名
- `XxxFragment`: Fragment 视图控制器
- `XxxViewModel`: ViewModel
- `XxxRepository`: 数据仓库
- `XxxManager`: 业务管理器
- `XxxHelper`: 功能辅助类
- `XxxDelegate`: 委托类
- `XxxAdapter`: 适配器
- `XxxDialog`: 对话框封装

### 包结构建议
```
com.example.app/
├── ui/
│   ├── map/
│   │   ├── MapFragment.java
│   │   ├── MapViewModel.java
│   │   ├── MapController.java
│   │   ├── dialogs/
│   │   └── adapters/
│   └── ...
├── data/
│   ├── model/
│   ├── repository/
│   └── api/
├── domain/
│   ├── usecase/
│   └── manager/
└── util/
```

---

## 代码审查检查清单

在编写或审查代码时，请检查：

- [ ] 单个文件是否超过 300 行？
- [ ] 类是否承担了多项不相关的职责？
- [ ] 是否有可以提取的对话框逻辑？
- [ ] 是否有可以提取的监听器实现？
- [ ] 是否有可以提取的工具方法？
- [ ] 是否遵循了单一职责原则？
- [ ] 包结构是否清晰合理？

---

## 重构优先级

1. **高优先级**: 超过 500 行的文件必须立即拆分
2. **中优先级**: 对话框逻辑必须独立封装
3. **低优先级**: 工具方法提取到工具类

---

记住：**好的代码架构就像猫咪整理好的玩具箱，每个玩具都有自己的位置，找起来方便又舒服～喵！**
