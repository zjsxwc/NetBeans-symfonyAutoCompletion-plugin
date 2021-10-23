Netbeans 上 Symfony3.x 对php、xml、yml、yaml 文件自动补全的插件。

>好像Symfony4 之后没有Bundle了，也就不需要对这些容器服务别名额外的支持，直接用php语法就行，所以对我来说支持到Symfony3就够用了。
>
>如果Symfony5要使用这个插件就必须多干一步就是如下创建个软链接：
>
>```
> cd var/cache/dev/
> ln -s Container2no6kMq/App_KernelDevDebugContainer.php  appDevDebugProjectContainer.php
>```
>把Symfony5编译后的`var/cache/dev/Container<build_hash_suffix>/App_KernelDevDebugContainer.php`软链接到`var/cache/dev/appDevDebugProjectContainer.php`,
>
>让插件能够读取到appDevDebugProjectContainer.php文件。
>

<p align="center">
    <a href="https://i.loli.net/2020/05/19/CRNiKa5bLZu7T6y.png"  target="_blank">
      <img src="https://i.loli.net/2020/05/19/CRNiKa5bLZu7T6y.png" width=600>
    </a>
</p>

<p align="center">
    <a href="https://i.loli.net/2020/05/22/cwRJU1dgTVv5Kfl.gif" target="_blank">
      <img src="https://i.loli.net/2020/05/22/cwRJU1dgTVv5Kfl.gif" width=600>
    </a>
</p>

#### 使用
0. 首次安装插件后，需要打开项目目录后，更新缓存，重启Netbeans

1. 添加一个跳出自动补全的快捷键 Ctrl+H

    <img src="https://raw.githubusercontent.com/zjsxwc/NetBeans-symfonyAutoCompletion-plugin/master/pic1.png" width=800>
    
2. 在要补全的文本后面放入光标，然后使用  Ctrl+H 跳出自动补全对话框，有补全phpdoc类型注释的需求可以shift+enter选择后会补全phpdoc注释

    <img src="https://raw.githubusercontent.com/zjsxwc/NetBeans-symfonyAutoCompletion-plugin/master/pic2.png" width=800>
    
    <img src="https://raw.githubusercontent.com/zjsxwc/NetBeans-symfonyAutoCompletion-plugin/master/pic3.png" width=800>

