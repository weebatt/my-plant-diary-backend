package com.myplantdiary.apps.telegram.telegram

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRegistryRepository : JpaRepository<ChatRegistryEntity, String>

