# JavaBDD_Application

BDD：Binary Decision Diagram，二元决策图，是一种用于表示布尔函数的数据结构
JavaBDD：用于操作BDD的Java库，[官网](http://javabdd.sourceforge.net/)

本仓库的代码呈现了一部分 JavaBDD 的应用，包括：
- 电路正确性验证
- 布尔函数运算
- CTL模型检验

其他说明：
- 基于 SpringBoot 框架实现，使用 Graphviz 可视化 BDD；
- 包含了 JavaBDD 库的用法笔记和补充资料；
- 实现了一种基于 JavaBDD 库的布尔表达式求值算法；
- 实现了一种将复杂 CTL 公式解析为 SemanticTree 语义树结构的算法；
- 实现了一种基于 JavaBDD 库求取前驱状态集的算法，以及基于该算法和 SemanticTree 语义树结构实现模型检验的算法；
- 实现了基于 Graphviz 引擎的对复杂系统状态机和 CTL 公式解析后的语义树进行可视化的功能

ps：
- 更多相关文章：[简书主页](https://www.jianshu.com/u/083f118d6003)

- Web界面参考：[shapefactory](https://shapefactory.co/)