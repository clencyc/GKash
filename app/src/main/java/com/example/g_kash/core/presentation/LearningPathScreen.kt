package com.example.g_kash.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(
    categoryId: String,
    onNavigateBack: () -> Unit
) {
    val category = getLearningPathData(categoryId)
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button
        LearningPathHeader(
            title = category.title,
            onNavigateBack = onNavigateBack
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Category overview card
                CategoryOverviewCard(category = category)
            }
            
            item {
                Text(
                    text = "Learning Modules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(category.modules) { module ->
                LearningModuleCard(
                    module = module,
                    onClick = { /* Handle module click */ },
                    onLessonClick = { lesson -> selectedLesson = lesson }
                )
            }
            
            item {
                Text(
                    text = "Practice Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(category.exercises) { exercise ->
                PracticeExerciseCard(
                    exercise = exercise,
                    onClick = { /* Handle exercise click */ }
                )
            }
            
            item {
                // Progress tracking
                ProgressSection(progress = category.progress)
            }
        }
    }

    selectedLesson?.let { lesson ->
        LessonPlayerOverlay(
            lesson = lesson,
            onDismiss = { selectedLesson = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun CategoryOverviewCard(category: LearningPath) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Modules",
                    value = "${category.modules.size}",
                    icon = Icons.Default.MenuBook
                )
                StatItem(
                    label = "Duration",
                    value = category.estimatedTime,
                    icon = Icons.Default.AccessTime
                )
                StatItem(
                    label = "Level",
                    value = category.difficulty,
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun LearningModuleCard(
    module: LearningModule,
    onClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Module status indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (module.status) {
                            ModuleStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                            ModuleStatus.LOCKED -> MaterialTheme.colorScheme.outline
                        },
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (module.status) {
                        ModuleStatus.COMPLETED -> Icons.Default.CheckCircle
                        ModuleStatus.IN_PROGRESS -> Icons.Default.PlayCircleOutline
                        ModuleStatus.LOCKED -> Icons.Default.Lock
                    },
                    contentDescription = module.status.name,
                    tint = when (module.status) {
                        ModuleStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimary
                        ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondary
                        ModuleStatus.LOCKED -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${module.duration} • ${module.lessons.size} lessons",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (module.lessons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    module.lessons.forEach { lesson ->
                        LessonRow(lesson = lesson, onClick = { onLessonClick(lesson) })
                    }
                }
            }
            
            if (module.status != ModuleStatus.LOCKED) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open module",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (lesson.type) {
                        LessonType.VIDEO -> "Video"
                        LessonType.ARTICLE -> "Article"
                        LessonType.INTERACTIVE -> "Interactive"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = when (lesson.type) {
                    LessonType.VIDEO -> Icons.Default.PlayCircleOutline
                    LessonType.ARTICLE -> Icons.Default.MenuBook
                    LessonType.INTERACTIVE -> Icons.Default.AutoAwesome
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonPlayerOverlay(lesson: Lesson, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(lesson.title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
            if (lesson.type == LessonType.VIDEO) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    VideoPlayer(
                        url = lesson.content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                }
            } else {
                Text(
                    text = lesson.content,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoPlayer(url: String, modifier: Modifier = Modifier.fillMaxWidth().height(220.dp)) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val embedUrl = remember(url) { toEmbedUrl(url) }
    val html = remember(embedUrl) {
        """
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
        <body style="margin:0;padding:0;background-color:black;">
        <iframe
            width="100%"
            height="100%"
            src="$embedUrl"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen>
        </iframe>
        </body></html>
        """.trimIndent()
    }

    val activity = remember { context as? android.app.Activity }

    // Root container to host WebView and potential fullscreen custom view
    val rootContainer = remember {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.BLACK)
        }
    }

    // The WebView instance
    val webView = remember {
        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            keepScreenOn = true
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
            // Prefer hardware layer in overlay; fallback to software only if needed.
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            webViewClient = object : WebViewClient() {
                private fun shouldStayInFrame(targetUrl: String): Boolean {
                    return targetUrl.contains("/embed/") || targetUrl.contains("/iframe_api")
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                    val target = request?.url ?: return false
                    val host = target.host.orEmpty()
                    val allowed = host.contains("youtube.com") ||
                        host.contains("youtube-nocookie.com") ||
                        host.contains("googlevideo.com") ||
                        host.contains("google.com") ||
                        host.contains("gstatic.com") ||
                        host.contains("ytimg.com") ||
                        host.contains("ggpht.com") ||
                        host.contains("googleusercontent.com")

                    if (!allowed) return true

                    if (!shouldStayInFrame(target.toString())) {
                        view?.loadUrl(embedUrl)
                        return true
                    }

                    return false
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val target = url ?: return false
                    val allowed = target.contains("youtube.com") || target.contains("youtube-nocookie.com") || target.contains("googlevideo.com") || target.contains("google.com") || target.contains("gstatic.com") || target.contains("ytimg.com") || target.contains("ggpht.com") || target.contains("googleusercontent.com")

                    if (!allowed) return true

                    if (!shouldStayInFrame(target)) {
                        view?.loadUrl(embedUrl)
                        return true
                    }

                    return false
                }
            }
            // Fullscreen custom view handling
            webChromeClient = object : WebChromeClient() {
                private var customView: View? = null
                private var customViewCallback: CustomViewCallback? = null

                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) {
                        onHideCustomView()
                    }
                    customView = view
                    customViewCallback = callback
                    // Enter immersive fullscreen
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    activity?.window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                    // Attach the custom view on top of WebView
                    view?.let {
                        rootContainer.addView(
                            it,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                        this@apply.visibility = View.GONE
                    }
                }

                override fun onHideCustomView() {
                    activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    customView?.let {
                        rootContainer.removeView(it)
                    }
                    customViewCallback?.onCustomViewHidden()
                    customView = null
                    customViewCallback = null
                    this@apply.visibility = View.VISIBLE
                }
            }
        }
    }

    AndroidView(
        factory = {
            // Ensure WebView is attached inside the root container
            if (webView.parent == null) {
                rootContainer.addView(
                    webView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            rootContainer
        },
        update = {
            webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "utf-8", null)
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.clearHistory()
                webView.onPause()
                webView.pauseTimers()
                webView.destroy()
            } catch (_: Throwable) {
                // ignore cleanup exceptions
            }
        }
    }
}

private fun toEmbedUrl(url: String): String {
    val cleaned = url.substringBefore('&').substringBefore('?')
    val videoId = when {
        cleaned.contains("youtu.be/") -> cleaned.substringAfter("youtu.be/")
        cleaned.contains("watch?v=") -> cleaned.substringAfter("watch?v=")
        else -> cleaned
    }
    return "https://www.youtube-nocookie.com/embed/$videoId?rel=0&modestbranding=1&playsinline=1&autoplay=1"
}

@Composable
fun PracticeExerciseCard(
    exercise: PracticeExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Badge(
                    containerColor = when (exercise.type) {
                        ExerciseType.QUIZ -> MaterialTheme.colorScheme.primary
                        ExerciseType.CALCULATOR -> MaterialTheme.colorScheme.secondary
                        ExerciseType.SCENARIO -> MaterialTheme.colorScheme.tertiary
                    }
                ) {
                    Text(
                        text = exercise.type.name,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Duration",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = exercise.estimatedTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (exercise.isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSection(progress: LearningProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress.completionPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress.completionPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progress.completedModules}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = progress.timeSpent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Time Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progress.streak}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Day Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Data classes for learning path
data class LearningPath(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val estimatedTime: String,
    val difficulty: String,
    val modules: List<LearningModule>,
    val exercises: List<PracticeExercise>,
    val progress: LearningProgress
)

data class LearningModule(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val lessons: List<Lesson>,
    val status: ModuleStatus
)

data class Lesson(
    val id: String,
    val title: String,
    val content: String,
    val type: LessonType
)

data class PracticeExercise(
    val id: String,
    val title: String,
    val description: String,
    val estimatedTime: String,
    val type: ExerciseType,
    val isCompleted: Boolean
)

data class LearningProgress(
    val completionPercentage: Float,
    val completedModules: Int,
    val totalModules: Int,
    val timeSpent: String,
    val streak: Int
)

enum class ModuleStatus { COMPLETED, IN_PROGRESS, LOCKED }
enum class LessonType { VIDEO, ARTICLE, INTERACTIVE }
enum class ExerciseType { QUIZ, CALCULATOR, SCENARIO }

// Mock data function
fun getLearningPathData(categoryId: String): LearningPath {
    return when (categoryId) {
        "get_started" -> LearningPath(
            id = "get_started",
            title = "Get Started",
            description = "Learn the basics of personal finance and money management",
            icon = Icons.Default.Home,
            estimatedTime = "2 hours",
            difficulty = "Beginner",
            modules = listOf(
                LearningModule(
                    id = "basics",
                    title = "Financial Basics",
                    description = "Understanding money, income, and expenses",
                    duration = "30 min",
                    lessons = listOf(
                        Lesson(
                            id = "video_intro_finance",
                            title = "Intro to Personal Finance",
                            content = "https://youtu.be/_xKNiIvygkM?si=EkC9FQy4hbpzank_",
                            type = LessonType.VIDEO
                        ),
                        Lesson(
                            id = "video_budgeting",
                            title = "Budgeting Basics",
                            content = "https://youtu.be/hxg_rbaf0pg?si=R6-hWvcs7kmCfbHQ",
                            type = LessonType.VIDEO
                        ),
                        Lesson(
                            id = "video_saving",
                            title = "Saving Strategies",
                            content = "https://youtu.be/KUpYBR7d6is?si=MYf7n0dJWJIEdKKs",
                            type = LessonType.VIDEO
                        )
                    ),
                    status = ModuleStatus.COMPLETED
                ),
                LearningModule(
                    id = "goals",
                    title = "Setting Financial Goals",
                    description = "How to set and achieve your money goals",
                    duration = "25 min",
                    lessons = listOf(),
                    status = ModuleStatus.IN_PROGRESS
                ),
                LearningModule(
                    id = "tracking",
                    title = "Tracking Your Money",
                    description = "Tools and methods to monitor your finances",
                    duration = "35 min",
                    lessons = listOf(),
                    status = ModuleStatus.LOCKED
                )
            ),
            exercises = listOf(
                PracticeExercise(
                    id = "budget_quiz",
                    title = "Budgeting Quiz",
                    description = "Test your understanding of basic budgeting concepts",
                    estimatedTime = "10 min",
                    type = ExerciseType.QUIZ,
                    isCompleted = true
                ),
                PracticeExercise(
                    id = "goal_calculator",
                    title = "Goal Planning Calculator",
                    description = "Calculate how much to save for your goals",
                    estimatedTime = "15 min",
                    type = ExerciseType.CALCULATOR,
                    isCompleted = false
                )
            ),
            progress = LearningProgress(
                completionPercentage = 0.4f,
                completedModules = 1,
                totalModules = 3,
                timeSpent = "45m",
                streak = 3
            )
        )
        
        "saving_basics" -> LearningPath(
            id = "saving_basics",
            title = "Saving Basics",
            description = "Master the art of saving money and building wealth",
            icon = Icons.Default.AccountBalance,
            estimatedTime = "3 hours",
            difficulty = "Beginner",
            modules = listOf(
                LearningModule(
                    id = "emergency_fund",
                    title = "Emergency Fund",
                    description = "Building your financial safety net",
                    duration = "40 min",
                    lessons = listOf(),
                    status = ModuleStatus.COMPLETED
                ),
                LearningModule(
                    id = "savings_strategies",
                    title = "Savings Strategies",
                    description = "Effective ways to save more money",
                    duration = "45 min",
                    lessons = listOf(),
                    status = ModuleStatus.IN_PROGRESS
                )
            ),
            exercises = listOf(
                PracticeExercise(
                    id = "emergency_calculator",
                    title = "Emergency Fund Calculator",
                    description = "Calculate your ideal emergency fund size",
                    estimatedTime = "10 min",
                    type = ExerciseType.CALCULATOR,
                    isCompleted = false
                )
            ),
            progress = LearningProgress(
                completionPercentage = 0.6f,
                completedModules = 1,
                totalModules = 2,
                timeSpent = "1h 20m",
                streak = 5
            )
        )
        
        // Add more categories as needed
        else -> LearningPath(
            id = categoryId,
            title = "Learning Path",
            description = "Expand your financial knowledge",
            icon = Icons.Default.School,
            estimatedTime = "1 hour",
            difficulty = "Beginner",
            modules = listOf(),
            exercises = listOf(),
            progress = LearningProgress(0f, 0, 0, "0m", 0)
        )
    }
}