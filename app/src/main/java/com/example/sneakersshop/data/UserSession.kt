package com.example.sneakersshop.data

object UserSession {
    // id пользователя из auth.users (uuid)
    var userId: String? = null

    // access_token, который вернул Supabase при signIn
    var accessToken: String? = null
}
