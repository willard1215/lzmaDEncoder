# lzmaDEncoder

특정 게임에서 사용되는 압축 및 압축해제 툴입니다.
[이 프로젝트](https://github.com/L-Leite/csovxl)를 Java로 포팅해 제작되었습니다.

Compression and decompression tools used in certain game.
Built by port [this project](https://github.com/L-Leite/csovxl) to Java.

## Requirements

-   Java 17 or higher (JDK)
-   Maven 3.6+ or Gradle 7+ (if building from source)
-   Compatible operating system: Windows, Linux, macOS

## Usage

압축해제(decompression)

```bash
java -jar vxl.jar d [inputname.vxl] [outputname.dec_data]
```

압축(compression)

```bash
java -jar vxl.jar c [outputname.dec_data] [inputname.vxl]
```

## License

-   GNU GPLv3 license
