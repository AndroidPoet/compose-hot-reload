import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun SomeWidget() {
    Column {
        Text("👋 Hello from 'widgets'", fontSize = 24.0.sp)
    }
}
