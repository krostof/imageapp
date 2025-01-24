module com.example.imageapp {
    requires javafx.controls;
    requires javafx.media;
    requires javafx.swing;
    requires java.desktop;
    requires opencv;
    requires org.apache.logging.log4j;
    requires static lombok;
    requires jdk.compiler;
    requires java.base;
    exports org.example.appinterface;

    exports org.example.projectaverage;
}
