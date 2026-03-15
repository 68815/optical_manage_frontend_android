# MVVM 架构规范

## 概述

本项目采用标准的 **MVVM (Model-View-ViewModel)** 架构模式，结合 **Repository 模式** 和 **LiveData** 进行数据管理。

## 架构分层

### 1. View 层 (UI 层)

**职责**：
- 负责视图渲染和用户交互
- 观察 ViewModel 的 LiveData 数据变化
- 不直接处理业务逻辑

**实现**：
- `Activity` / `Fragment` 作为视图容器
- 使用 `ViewModelProvider` 获取 ViewModel 实例
- 通过 `observe()` 方法观察 LiveData

**代码示例**：
```java
public class MapFragment extends Fragment {
    private MapViewModel viewModel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用 Factory 创建 ViewModel
        MapViewModelFactory factory = new MapViewModelFactory(apiService);
        viewModel = new ViewModelProvider(this, factory).get(MapViewModel.class);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 观察 LiveData
        viewModel.getResourcePoints().observe(getViewLifecycleOwner(), points -> {
            // 更新 UI
            updateMarkers(points);
        });
        
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showToast(error);
            }
        });
    }
}
```

### 2. ViewModel 层

**职责**：
- 管理 UI 相关的数据
- 处理业务逻辑
- 调用 Repository 进行数据操作
- 使用 LiveData 暴露数据给 View 层

**规则**：
- 继承 `androidx.lifecycle.ViewModel`
- 不持有 View 的引用
- 不直接进行网络请求或数据库操作
- 使用 `MutableLiveData` 内部修改，暴露 `LiveData` 给外部

**代码示例**：
```java
public class MapViewModel extends ViewModel {
    private final ResourcePointRepository repository;
    
    // 内部可变数据
    private final MutableLiveData<List<ResourcePoint>> resourcePoints = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public MapViewModel(ResourcePointRepository repository) {
        this.repository = repository;
    }
    
    // 对外暴露不可变 LiveData
    public LiveData<List<ResourcePoint>> getResourcePoints() {
        return resourcePoints;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    // 业务逻辑方法
    public void loadResourcePoints() {
        repository.loadAllResourcePoints();
    }
    
    public void createResourcePoint(ResourcePoint point) {
        repository.createResourcePoint(point);
    }
}
```

### 3. Repository 层 (数据层)

**职责**：
- 统一的数据访问接口
- 管理数据来源（网络、本地数据库等）
- 使用 LiveData 暴露数据
- 处理数据转换和缓存

**规则**：
- 不持有 View 或 Context 的引用
- 不直接操作 UI（不显示 Toast、Dialog 等）
- 通过 LiveData 通知数据变化
- 单一职责：每个 Repository 管理一种数据类型

**代码示例**：
```java
public class ResourcePointRepository {
    private final ApiService apiService;
    
    private final MutableLiveData<List<ResourcePoint>> resourcePoints = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public ResourcePointRepository(ApiService apiService) {
        this.apiService = apiService;
    }
    
    public LiveData<List<ResourcePoint>> getResourcePoints() {
        return resourcePoints;
    }
    
    public void loadAllResourcePoints() {
        apiService.getAllResourcePoints().enqueue(new Callback<ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ResourcePoint>>> call, 
                                  Response<ApiResponse<List<ResourcePoint>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resourcePoints.setValue(response.body().getData());
                } else {
                    errorMessage.setValue("加载失败");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<ResourcePoint>>> call, Throwable t) {
                errorMessage.setValue("网络错误：" + t.getMessage());
            }
        });
    }
}
```

### 4. Model 层

**职责**：
- 定义数据实体类
- 包含数据结构和基本业务逻辑

**位置**：
- `model/entity/` - 实体类
- `model/request/` - 请求 DTO
- `model/response/` - 响应 DTO

## 包结构规范

```
cn.edu.ncepu.optical_manage/
├── data/
│   └── repository/          # 数据仓库层
│       ├── ResourcePointRepository.java
│       └── CableSegmentRepository.java
├── model/
│   ├── entity/              # 实体类
│   │   ├── ResourcePoint.java
│   │   └── CableSegment.java
│   ├── request/             # 请求模型
│   └── response/            # 响应模型
├── api/                     # 网络层
│   ├── ApiClient.java
│   └── ApiService.java
├── ui/                      # UI 层
│   ├── MapFragment.java
│   ├── MapViewModel.java
│   ├── MapViewModelFactory.java
│   ├── dialogs/             # 对话框
│   ├── adapters/            # 适配器
│   └── helper/              # 辅助类
└── manager/                 # 管理器（仅地图相关）
    ├── ResourcePointManager.java
    └── CableSegmentManager.java
```

## 命名规范

| 组件 | 命名规则 | 示例 |
|------|----------|------|
| ViewModel | `XxxViewModel` | `MapViewModel` |
| Repository | `XxxRepository` | `ResourcePointRepository` |
| Factory | `XxxViewModelFactory` | `MapViewModelFactory` |
| LiveData (Mutable) | `xxx` / `mXxx` | `resourcePoints` |
| LiveData (Expose) | `getXxx()` | `getResourcePoints()` |

## LiveData 使用规范

### 1. 数据暴露原则

```java
// 内部使用 MutableLiveData
private final MutableLiveData<List<ResourcePoint>> resourcePoints = new MutableLiveData<>();

// 对外暴露不可变的 LiveData
public LiveData<List<ResourcePoint>> getResourcePoints() {
    return resourcePoints;
}
```

### 2. 观察时机

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // 使用 getViewLifecycleOwner() 确保生命周期安全
    viewModel.getData().observe(getViewLifecycleOwner(), data -> {
        // 更新 UI
    });
}
```

### 3. 错误处理

```java
// Repository 中设置错误
errorMessage.setValue("加载失败：" + t.getMessage());

// View 中观察并显示
viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
    if (error != null) {
        showToast(error);
        viewModel.clearError(); // 消费错误
    }
});
```

## 依赖注入

使用 ViewModelFactory 进行依赖注入：

```java
public class MapViewModelFactory implements ViewModelProvider.Factory {
    private final ApiService apiService;
    
    public MapViewModelFactory(ApiService apiService) {
        this.apiService = apiService;
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MapViewModel.class)) {
            ResourcePointRepository repository = new ResourcePointRepository(apiService);
            return (T) new MapViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
```

## 禁止事项

1. **ViewModel 中禁止**：
   - 持有 View 或 Context 引用
   - 直接操作 UI（Toast、Dialog 等）
   - 进行网络请求或数据库操作（应通过 Repository）

2. **Repository 中禁止**：
   - 持有 View 或 Context 引用
   - 直接操作 UI
   - 处理业务逻辑（只负责数据获取）

3. **View 中禁止**：
   - 直接调用 Repository
   - 直接进行网络请求
   - 存储 UI 状态（应使用 ViewModel）

## 最佳实践

1. **单一职责**：每个类只负责一项职责
2. **数据驱动 UI**：通过观察 LiveData 自动更新界面
3. **生命周期感知**：使用 `getViewLifecycleOwner()` 观察数据
4. **错误统一处理**：在 Repository 中捕获异常，通过 LiveData 通知 View
5. **状态管理**：使用 ViewModel 保存 UI 状态，避免配置变更丢失数据

## 示例流程

### 加载数据流程

```
1. View: viewModel.loadData()
2. ViewModel: repository.loadData()
3. Repository: apiService.getData() (Retrofit)
4. Repository: data.setValue(result)
5. View: observe(data) -> update UI
```

### 用户操作流程

```
1. View: 用户点击按钮
2. View: viewModel.doSomething()
3. ViewModel: 验证输入，调用 repository
4. Repository: 执行网络请求
5. Repository: 更新 LiveData
6. View: 自动收到通知，更新 UI
```

---

**记住**：MVVM 的核心是 **数据驱动视图**，保持各层职责清晰，代码就会像猫咪一样优雅～喵！🐱
