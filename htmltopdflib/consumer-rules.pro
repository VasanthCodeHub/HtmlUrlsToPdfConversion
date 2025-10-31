# Consumer ProGuard rules for library users

# Keep public API
-keep public class com.example.htmltopdflib.HtmlToPdfConverter { *; }
-keep public class com.example.htmltopdflib.HtmlToPdfConfig { *; }
-keep public class com.example.htmltopdflib.HtmlToPdfConfig$Builder { *; }
-keep public class com.example.htmltopdflib.HtmlToPdfResult { *; }
-keep public interface com.example.htmltopdflib.HtmlToPdfCallback { *; }
