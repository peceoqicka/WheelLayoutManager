# WheelLayoutManager

![Bintray](https://img.shields.io/badge/JCenter-v1.0.2-blue.svg)

## 简介

> A customized LayoutManager used to make a wheel-like DatePicker by using RecyclerView.
It makes a easy way to create a DatePicker and style it.

> 一个用于实现自定义的DatePicker的LayoutManager，通过RecyclerView来轻松实现内容自定义和样式自定义。

![WheelLayoutManager](/images/wheellayoutmanager.gif)
![Selection Change](/images/wheellayoutmanager-2.gif)
![Infinite Scrolling](/images/wheellayoutmanager-3.gif)

## 引入

```groovy
implementation 'com.peceoqicka:wheellayoutmanager:1.0.2'
```

## 使用

```kotlin
recyclerView.layoutManager = WheelLayoutManager(5, false)
```

### 参数说明
* visibleCount 显示的Item数量，必须为大于等于3的奇数
* infinity 是否为无线循环模式，默认为false 

## 更新记录

* 1.0.2 增加无线循环模式
* 1.0.1 修复快速滚动停止后偏移像素的问题
* 1.0.0 第一版