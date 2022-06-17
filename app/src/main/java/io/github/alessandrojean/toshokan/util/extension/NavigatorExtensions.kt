package io.github.alessandrojean.toshokan.util.extension

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

fun Navigator.push(block: () -> Screen) = this.push(block.invoke())
