# Object Detection module for Android Things

## Overview

This library provide object detection function to Android Things. It is using coco-ssd-mobilenet-v1 model for object detection.
In use this library, Your application should be prepare model that was already quantization.  please see usage.


## Usage

### Prepare setting.

1. Add bintray repository to your project root gradle file.

```gradke
repositories {
        google()
        jcenter()
        maven {
            url 'https://dl.bintray.com/masato-ka/android-things-support'
        }
}
``` 

2. Add to dependencies

```
dependencies {
    implementation 'ka.masato.library.ai:ssddetection:0.1.0'
}
```

3. Model download

Fortunataly, TensorFlow proper repository provided coco-ssd-mobilenet-v1 model that was already quantization.

At first, Add new assets folder and assets file setting.

```
android {
     .
     .
     .
     .
    
    sourceSets {
        main {
            assets.srcDirs = ['./src/main/assets']
        }
    }
    aaptOptions {
        noCompress "tflite"
    }
}
```


And create download gradle script in app module directory.

* download-models.gradle

``` 
def models = ['coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip']

def MODEL_URL = 'https://storage.googleapis.com/download.tensorflow.org/models/tflite'

buildscript {
    repositories{
        jcenter()
    }
    dependencies{
        classpath 'de.undercouch:gradle-download-task:3.2.0'
    }
}

import de.undercouch.gradle.tasks.download.Download

task downloadFile(type: Download){
    for (f in models) {
        def modelUrl = MODEL_URL + "/" + f
        println "Downloading ${f} from ${modelUrl}"
        src modelUrl
    }

    dest new File(project.ext.TMP_DIR)
    overwrite true
}

task extractModels(type: Copy) {
    for (f in models) {
        def localFile = f.split("/")[-1]
        from zipTree(project.ext.TMP_DIR + '/' + localFile)
    }

    into file(project.ext.ASSET_DIR)
    fileMode  0644
    exclude '**/LICENSE'

    def needDownload = false
    for (f in models) {
        def localFile = f.split("/")[-1]
        if (!(new File(project.ext.TMP_DIR + '/' + localFile)).exists()) {
            needDownload = true
        }
    }

    if (needDownload) {
        dependsOn downloadFile
    }
}

tasks.whenTaskAdded { task ->
    if (task.name == 'assembleDebug') {
        task.dependsOn 'extractModels'
    }
    if (task.name == 'assembleRelease') {
        task.dependsOn 'extractModels'
    }
}
```

Create directory in App module directory.

 * ```APP_DIR/src/main/assets```
 * ```APP_DIR/build/downloads```
 
 call download-models.gradle from build.gradle
 
 ```gradle

 project.ext.ASSET_DIR = projectDir.toString() + '/src/main/assets'
 project.ext.TMP_DIR   = project.buildDir.toString() + '/downloads'
 apply from: "download-models.gradle

```

### Sample code.

```java

private MultiObjectDetection mMuitiObjectDetection = MultiObjectDetection.getInstance();
mMultiObjectDetection.initializing(context, MODEL_FILE_PATH, LABEL_FILE_PATH);
ArrayList<Recognition> result = mMultiObjectDeteciton.doRecognize(bitmap, DETECT_OBJECT_NUMBER);
Lod.d("LOG", "Result:" + result.toString());

```


## LICENSE

MIT License

## Author

Twitter: @masato-ka
E-mail: jp6uzv@gmail.com