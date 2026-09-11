# SmartSpend ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# SQLCipher
-keep class net.zetetic.database.sqlcipher.** { *; }
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**

# Hilt / Dagger
-keep class com.smartspend.app.**_Factory { *; }
-keep class com.smartspend.app.**_MembersInjector { *; }

# Security Crypto
-keep class androidx.security.crypto.** { *; }
