package io.github.alessandrojean.toshokan.util.extension

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

fun Navigator.push(block: () -> Screen) = push(block.invoke())

fun Navigator.replace(block: () -> Screen) = replace(block.invoke())
