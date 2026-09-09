package com.catsdevs.graderun

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.documentfile.provider.DocumentFile

class MainActivity : Activity() {
    private lateinit var name: EditText
    private lateinit var pkg: EditText
    private lateinit var status: TextView
    private var folder: DocumentFile? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        fun add(v: android.view.View) = root.addView(v, LinearLayout.LayoutParams(-1, -2))

        add(TextView(this).apply { text = "GradleRun"; textSize = 30f })
        add(TextView(this).apply { text = "Создание Android Gradle-проектов из выбранной папки"; textSize = 16f })
        name = EditText(this).apply { hint = "Название проекта"; setText("MyApp"); singleLine = true }
        add(name)
        pkg = EditText(this).apply { hint = "Package name"; setText("com.example.myapp"); singleLine = true }
        add(pkg)
        add(Button(this).apply { text = "1. Выбрать папку"; setOnClickListener { pickFolder() } })
        add(Button(this).apply { text = "2. Создать проект"; setOnClickListener { generate() } })
        status = TextView(this).apply { text = "Выбери папку и нажми «Создать проект»."; textSize = 16f }
        add(status)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun pickFolder() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }, REQUEST_FOLDER)
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (request == REQUEST_FOLDER && result == RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (_: Exception) {}
            folder = DocumentFile.fromTreeUri(this, uri)
            status.text = "Папка: ${folder?.name ?: uri}"
        }
    }

    private fun generate() {
        val root = folder
        if (root == null) { status.text = "Сначала выбери папку."; return }
        val project = name.text.toString().trim().ifEmpty { "MyApp" }
        val applicationId = pkg.text.toString().trim().ifEmpty { "com.example.myapp" }
        if (!applicationId.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*"))) {
            status.text = "Неверный package name."; return
        }
        try {
            write(root, "settings.gradle.kts", """import org.gradle.api.initialization.resolve.RepositoriesMode
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "$project"
include(":app")
""")
            write(root, "build.gradle.kts", """plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}
""")
            write(root, "gradle.properties", "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8\nandroid.useAndroidX=true\n")
            write(root, ".gitignore", ".gradle/\n.idea/\nlocal.properties\n/build/\n*.iml\n")
            write(root, "app/build.gradle.kts", """plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "$applicationId"
    compileSdk = 35
    defaultConfig { applicationId = "$applicationId"; minSdk = 23; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
""")
            write(root, "app/src/main/AndroidManifest.xml", """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
 <application android:theme="@style/AppTheme" android:label="$project">
  <activity android:name=".MainActivity" android:exported="true">
   <intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter>
  </activity>
 </application>
</manifest>
""")
            write(root, "app/src/main/res/values/styles.xml", """<?xml version="1.0" encoding="utf-8"?><resources><style name="AppTheme" parent="android:style/Theme.Material.Light.NoActionBar"><item name="android:fontFamily">sans</item></style></resources>""")
            val source = "app/src/main/java/${applicationId.replace('.', '/')}/MainActivity.kt"
            write(root, source, """package $applicationId
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class MainActivity : Activity() {
 override fun onCreate(state: Bundle?) { super.onCreate(state); setContentView(TextView(this).apply { text = "Hello from GradleRun!"; textSize = 24f; setPadding(32,32,32,32) }) }
}
""")
            status.text = "Готово! Проект $project создан.\nТеперь открой эту папку в Android Studio или другом Gradle-совместимом IDE."
        } catch (e: Exception) { status.text = "Ошибка: ${e.message}" }
    }

    private fun write(root: DocumentFile, path: String, text: String) {
        val parts = path.split('/')
        var dir = root
        for (part in parts.dropLast(1)) dir = dir.findFile(part) ?: dir.createDirectory(part) ?: error("Не удалось создать $part")
        dir.findFile(parts.last())?.delete()
        val file = dir.createFile("text/plain", parts.last()) ?: error("Не удалось создать ${parts.last()}")
        contentResolver.openOutputStream(file.uri)?.use { it.write(text.toByteArray()) } ?: error("Не удалось записать ${parts.last()}")
    }

    companion object { const val REQUEST_FOLDER = 1001 }
}
