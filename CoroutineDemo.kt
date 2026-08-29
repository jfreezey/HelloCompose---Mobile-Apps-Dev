@Composable
fun DownloaderScreen(){
    var progress by remember { mutableFloatStateOf(0f)}
    var isDownloading by remember { mutableStateOf(false) }
    var isCancelled by remember { mutableStateOf(false) }
    var downloadJob: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text (
            text = when {
                isDownloading -> "Mengunduh..."
                isCancelled -> "Dibatalkan"
                progress >= 1f -> "Selesai"
                else -> "Siap"
            },
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator (
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("${(progress * 100).toInt()}%")
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp))
            {
                Button (
                    onClick = {
                        isCancelled = false
                        downloadJob = scope.launch {
                            isDownloading = true 
                            progress = 0f
                            repeat(100) {
                                delay(50)
                                progress += 0.01f
                            }
                            isDownloading = false
                        }
                    },
                    enabled = !isDownloading
                ) { Text("Mulai Download") }

                OutlinedButton (
                    onClick = {
                        downloadJob?.cancel()
                        isDownloading = false
                        isCancelled = true
                    },
                    enabled = isDownloading
                ) { Text("Cancel") }
            }
    }
}