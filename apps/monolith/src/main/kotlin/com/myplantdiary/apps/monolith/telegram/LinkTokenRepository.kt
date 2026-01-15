package com.myplantdiary.apps.monolith.telegram

import org.springframework.data.jpa.repository.JpaRepository

interface LinkTokenRepository : JpaRepository<LinkToken, String>

