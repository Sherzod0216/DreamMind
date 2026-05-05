package com.example.dreammind.core.data.fake

import com.example.dreammind.core.data.repository.DreamMindRepository
import com.example.dreammind.data.FakeDreamMindRepository as InMemoryFakeDreamMindRepository

object FakeDreamMindRepository : DreamMindRepository by InMemoryFakeDreamMindRepository
