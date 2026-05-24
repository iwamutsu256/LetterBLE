package com.example.letterble.di

/**
 * App-wide dependency entry point.
 *
 * Keep dependencies wired here with manual constructor injection until the
 * object graph becomes large enough to justify Hilt.
 */
interface AppContainer

object DefaultAppContainer : AppContainer
