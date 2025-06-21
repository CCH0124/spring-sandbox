## 比較項目
1. 冷快取建置時間（Cold Build）

- 第一次從零快取開始的完整建置時間。
- 衡量並行化與快取策略對初次建置的加速效果。

2. 熱快取建置時間（Warm Build）

- 緊接著冷快取後，再次執行建置所需的時間。
- 衡量快取命中率與精細化快取策略對重複建置的節省效果。

3. 映像檔最終大小（Image Size）

- `docker image ls` 列出的 SIZE 欄位。
- BuildKit 的 cache mount、刪除臨時檔等機制，通常能減少最終影像體積。

4. 映像層數量（Layer Count）

- `docker history <image>` 顯示的 layer 條目個數。
- 減少不必要的中間層，可優化 pull/push 與啟動速度。

5. 資源使用量（CPU／記憶體）

- 利用 time／hyperfine 報告中 user、sys，或在建置時監控 htop/top。
- 觀察 BuildKit 並行執行時是否更能充分利用多核心。

6. 網路下載量（Layers Pulled）

- BuildKit 延遲／惰性下載（lazy pulling）可以只拉取真正用到的 layers，節省網路 I/O。
- 用 `docker build --progress=plain` 查看 `pull log`。

7. 日誌與診斷易讀性

- 比較 legacy builder 的純文字 log 與 BuildKit 的併行 step 樹狀 log，或 JSON log（--progress=json）。
- 對 CI 故障排查的便利程度。



## 環境
1. 安裝 benchmark 

```
sudo apt update && sudo apt install -y hyperfine
```

2. 清空快取與 image

```bash
docker image rm test:legacy test:buildkit --force || true
docker buildx prune --all --force   # 如果你用 buildx
```

```bash
$ docker buildx build --platform linux/amd64 -t buildkit-spring:latest .
```

## 冷快取建置

```bash
$ time docker build -t spring-legacy:legacy --progress=plain .
#0 building with "default" instance using docker driver

#1 [internal] load build definition from Dockerfile
#1 transferring dockerfile: 553B done
#1 DONE 0.0s

#2 [internal] load metadata for docker.io/library/gradle:8.7-jdk21-alpine
#2 ...

#3 [auth] library/gradle:pull token for registry-1.docker.io
#3 DONE 0.0s

#4 [auth] library/eclipse-temurin:pull token for registry-1.docker.io
#4 DONE 0.0s

#5 [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine
#5 DONE 2.4s

#2 [internal] load metadata for docker.io/library/gradle:8.7-jdk21-alpine
#2 DONE 2.4s

#6 [internal] load .dockerignore
#6 transferring context: 2B done
#6 DONE 0.0s

#7 [stage-1 1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd
#7 resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd 0.0s done
#7 ...

#8 [internal] load build context
#8 transferring context: 46.62kB done
#8 DONE 0.0s

#7 [stage-1 1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd
#7 sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd 2.68kB / 2.68kB done
#7 sha256:62fa775039897e4420368514ba6c167741f6d45a0de9ff9125bee57e5aca8b75 1.94kB / 1.94kB done
#7 sha256:360e75d7612b35a3a65d4b7f2d5ecd735621389fa5e41dd7f55cb4c27878dc1c 3.98kB / 3.98kB done
#7 sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 0B / 3.64MB 0.2s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 0B / 16.18MB 0.2s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 0B / 53.06MB 0.2s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 6.29MB / 53.06MB 0.4s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 14.68MB / 53.06MB 0.6s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 2.10MB / 16.18MB 0.8s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 18.87MB / 53.06MB 0.8s
#7 sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 3.15MB / 3.64MB 0.9s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 3.15MB / 16.18MB 0.9s
#7 sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 3.64MB / 3.64MB 0.9s done
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 25.17MB / 53.06MB 1.0s
#7 extracting sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 0.1s
#7 sha256:e6744199aa66ab985e37e72924f1568a6751afa2c508c42a1b3b945f3a8850a7 0B / 126B 1.0s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 7.34MB / 16.18MB 1.2s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 31.46MB / 53.06MB 1.2s
#7 extracting sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 0.1s done
#7 sha256:e6744199aa66ab985e37e72924f1568a6751afa2c508c42a1b3b945f3a8850a7 126B / 126B 1.2s done
#7 sha256:cda86626eeb372589c3378d030f4522ba1b0c78ec58b1db87960fa4e5fcd3e34 0B / 2.28kB 1.2s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 8.39MB / 16.18MB 1.3s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 10.49MB / 16.18MB 1.4s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 35.65MB / 53.06MB 1.4s
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 12.58MB / 16.18MB 1.5s
#7 sha256:cda86626eeb372589c3378d030f4522ba1b0c78ec58b1db87960fa4e5fcd3e34 2.28kB / 2.28kB 1.4s done
#7 sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 16.18MB / 16.18MB 1.6s done
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 39.85MB / 53.06MB 1.7s
#7 extracting sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 42.99MB / 53.06MB 2.0s
#7 extracting sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 0.4s done
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 47.19MB / 53.06MB 2.2s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 50.33MB / 53.06MB 2.4s
#7 sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 53.06MB / 53.06MB 2.6s done
#7 extracting sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a
#7 extracting sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 0.8s done
#7 extracting sha256:e6744199aa66ab985e37e72924f1568a6751afa2c508c42a1b3b945f3a8850a7 done
#7 extracting sha256:cda86626eeb372589c3378d030f4522ba1b0c78ec58b1db87960fa4e5fcd3e34 done
#7 DONE 3.6s

#9 [builder 1/7] FROM docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1
#9 resolve docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1 0.0s done
#9 sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1 2.68kB / 2.68kB done
#9 sha256:b59e9873ba742a479cce77b1222861958ce5a6e621b95bf2ddcd3496e2d22b7c 2.90kB / 2.90kB done
#9 sha256:d3e26ecb87fb595e4952cc40f85668ec6b8df141786d550ba0867bd003bd22ba 7.72kB / 7.72kB done
#9 sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB 1.8s done
#9 sha256:a3fd38fd7cf5b8d60c92e1aa46a24527229fb51b451491d35a7028a8a1d0aba4 13.14MB / 13.14MB 2.7s done
#9 extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 0.1s done
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 34.60MB / 158.72MB 3.5s
#9 extracting sha256:a3fd38fd7cf5b8d60c92e1aa46a24527229fb51b451491d35a7028a8a1d0aba4 0.5s done
#9 sha256:45705fa60e5881f3a69ceb008964dcca0e72d626655d99b6d92e4c0834c7131b 717B / 717B 2.9s done
#9 sha256:2e377b89d6767f434b66aef2b55d4397fa1ca4ef205a16f2fc626005be867634 190B / 190B 2.8s done
#9 sha256:139b55c8532d30c022b37d343fee5e3b33a0e8e51e903d285b6624f144025cdd 1.31kB / 1.31kB 3.1s done
#9 sha256:4f4fb700ef54461cfa02571ae0db9a0dc1e0cdb5577484a6d75e68dc38e8acc1 32B / 32B 3.1s done
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 1.05MB / 34.88MB 3.5s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 0B / 134.21MB 3.5s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 3.15MB / 34.88MB 3.7s
#9 ...

#10 [stage-1 2/4] RUN addgroup -S spring && adduser -S spring -G spring
#10 DONE 0.4s

#9 [builder 1/7] FROM docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 42.99MB / 158.72MB 3.9s
#9 ...

#11 [stage-1 3/4] WORKDIR /app
#11 DONE 0.1s

#9 [builder 1/7] FROM docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 6.29MB / 34.88MB 4.1s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 8.39MB / 34.88MB 4.2s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 53.48MB / 158.72MB 4.4s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 10.49MB / 34.88MB 4.4s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 12.58MB / 34.88MB 4.6s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 63.96MB / 158.72MB 4.9s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 15.73MB / 34.88MB 4.9s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 18.87MB / 34.88MB 5.1s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 7.34MB / 134.21MB 5.1s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 73.40MB / 158.72MB 5.4s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 23.07MB / 34.88MB 5.4s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 26.21MB / 34.88MB 5.7s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 82.84MB / 158.72MB 5.9s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 29.36MB / 34.88MB 5.9s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 32.51MB / 34.88MB 6.1s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 91.23MB / 158.72MB 6.4s
#9 sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 34.88MB / 34.88MB 6.2s done
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 16.78MB / 134.21MB 6.4s
#9 sha256:7bda177df30edadfddb14fc7938185363d1a975fa06f625dfd41bba520bae1ee 0B / 54.91kB 6.4s
#9 sha256:7bda177df30edadfddb14fc7938185363d1a975fa06f625dfd41bba520bae1ee 54.91kB / 54.91kB 6.5s done
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 99.61MB / 158.72MB 6.3s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 26.21MB / 134.21MB 6.6s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 111.15MB / 158.72MB 6.8s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 33.55MB / 134.21MB 7.0s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 119.54MB / 158.72MB 7.3s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 41.94MB / 134.21MB 7.4s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 130.02MB / 158.72MB 7.9s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 51.38MB / 134.21MB 7.9s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 140.51MB / 158.72MB 8.4s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 59.77MB / 134.21MB 8.4s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 67.11MB / 134.21MB 8.8s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 152.04MB / 158.72MB 9.0s
#9 sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 158.72MB / 158.72MB 9.3s done
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 77.59MB / 134.21MB 9.4s
#9 extracting sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 0.1s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 87.03MB / 134.21MB 9.8s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 95.42MB / 134.21MB 10.0s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 106.95MB / 134.21MB 10.4s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 116.39MB / 134.21MB 10.6s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 123.73MB / 134.21MB 10.8s
#9 extracting sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 1.5s done
#9 extracting sha256:2e377b89d6767f434b66aef2b55d4397fa1ca4ef205a16f2fc626005be867634 done
#9 extracting sha256:45705fa60e5881f3a69ceb008964dcca0e72d626655d99b6d92e4c0834c7131b done
#9 extracting sha256:139b55c8532d30c022b37d343fee5e3b33a0e8e51e903d285b6624f144025cdd done
#9 extracting sha256:4f4fb700ef54461cfa02571ae0db9a0dc1e0cdb5577484a6d75e68dc38e8acc1 done
#9 extracting sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 132.12MB / 134.21MB 11.1s
#9 sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 134.21MB / 134.21MB 11.2s done
#9 extracting sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 1.2s done
#9 extracting sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156
#9 extracting sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 0.8s done
#9 extracting sha256:7bda177df30edadfddb14fc7938185363d1a975fa06f625dfd41bba520bae1ee done
#9 DONE 13.3s

#12 [builder 2/7] WORKDIR /workspace
#12 DONE 0.3s

#13 [builder 3/7] COPY --chown=gradle:gradle gradle gradle
#13 DONE 0.0s

#14 [builder 4/7] COPY --chown=gradle:gradle build.gradle .
#14 DONE 0.0s

#15 [builder 5/7] COPY --chown=gradle:gradle settings.gradle .
#15 DONE 0.0s

#16 [builder 6/7] COPY --chown=gradle:gradle src src
#16 DONE 0.0s

#17 [builder 7/7] RUN gradle clean build --no-daemon
#17 0.609 To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.7/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
#17 1.308 Daemon will be stopped at the end of the build 
#17 32.71 > Task :clean UP-TO-DATE
#17 36.10 > Task :compileJava
#17 36.10 > Task :processResources
#17 36.10 > Task :classes
#17 36.10 > Task :resolveMainClassName
#17 36.60 > Task :bootJar
#17 36.60 > Task :jar
#17 36.60 > Task :assemble
#17 42.40 > Task :compileTestJava
#17 42.40 > Task :processTestResources NO-SOURCE
#17 42.40 > Task :testClasses
#17 44.70 OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
#17 44.90 > Task :test
#17 44.90 > Task :check
#17 44.90 > Task :build
#17 44.90 
#17 44.90 BUILD SUCCESSFUL in 44s
#17 44.90 8 actionable tasks: 7 executed, 1 up-to-date
#17 DONE 45.4s

#18 [stage-1 4/4] COPY --from=builder /workspace/build/libs/*.jar app.jar
#18 DONE 0.1s

#19 exporting to image
#19 exporting layers 0.0s done
#19 writing image sha256:955f76b01fc29a9fb27e85f3dc677397e5e64a4860d004c31d46838725b83609 done
#19 naming to docker.io/library/spring-legacy:legacy done
#19 DONE 0.1s

real    1m2.002s
user    0m0.103s
sys     0m0.180s
```




```bash
$ time docker buildx build --platform linux/amd64 -t buildkit-spring:latest .
[+] Building 44.3s (19/19) FINISHED                                                                                                                                                                                                                                                    docker:default
 => [internal] load build definition from Dockerfile                                                                                                                                                                                                                                             0.0s
 => => transferring dockerfile: 553B                                                                                                                                                                                                                                                             0.0s
 => [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine                                                                                                                                                                                                                 2.1s
 => [internal] load metadata for docker.io/library/gradle:8.7-jdk21-alpine                                                                                                                                                                                                                       2.1s
 => [auth] library/eclipse-temurin:pull token for registry-1.docker.io                                                                                                                                                                                                                           0.0s
 => [auth] library/gradle:pull token for registry-1.docker.io                                                                                                                                                                                                                                    0.0s
 => [internal] load .dockerignore                                                                                                                                                                                                                                                                0.0s
 => => transferring context: 2B                                                                                                                                                                                                                                                                  0.0s
 => [builder 1/7] FROM docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1                                                                                                                                                        14.3s
 => => resolve docker.io/library/gradle:8.7-jdk21-alpine@sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1                                                                                                                                                                 0.0s
 => => sha256:d6ea1c746d8365fae41c70d5812c28c8fca88c905b69d5f9da57ad4cc0218ab1 2.68kB / 2.68kB                                                                                                                                                                                                   0.0s
 => => sha256:b59e9873ba742a479cce77b1222861958ce5a6e621b95bf2ddcd3496e2d22b7c 2.90kB / 2.90kB                                                                                                                                                                                                   0.0s
 => => sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8 3.41MB / 3.41MB                                                                                                                                                                                                   0.3s
 => => sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3 158.72MB / 158.72MB                                                                                                                                                                                              10.3s
 => => sha256:d3e26ecb87fb595e4952cc40f85668ec6b8df141786d550ba0867bd003bd22ba 7.72kB / 7.72kB                                                                                                                                                                                                   0.0s
 => => sha256:a3fd38fd7cf5b8d60c92e1aa46a24527229fb51b451491d35a7028a8a1d0aba4 13.14MB / 13.14MB                                                                                                                                                                                                 1.3s
 => => extracting sha256:4abcf20661432fb2d719aaf90656f55c287f8ca915dc1c92ec14ff61e67fbaf8                                                                                                                                                                                                        0.1s
 => => sha256:2e377b89d6767f434b66aef2b55d4397fa1ca4ef205a16f2fc626005be867634 190B / 190B                                                                                                                                                                                                       0.7s
 => => sha256:45705fa60e5881f3a69ceb008964dcca0e72d626655d99b6d92e4c0834c7131b 717B / 717B                                                                                                                                                                                                       0.9s
 => => sha256:139b55c8532d30c022b37d343fee5e3b33a0e8e51e903d285b6624f144025cdd 1.31kB / 1.31kB                                                                                                                                                                                                   1.2s
 => => sha256:4f4fb700ef54461cfa02571ae0db9a0dc1e0cdb5577484a6d75e68dc38e8acc1 32B / 32B                                                                                                                                                                                                         1.4s
 => => extracting sha256:a3fd38fd7cf5b8d60c92e1aa46a24527229fb51b451491d35a7028a8a1d0aba4                                                                                                                                                                                                        0.6s
 => => sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f 34.88MB / 34.88MB                                                                                                                                                                                                 5.6s
 => => sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156 134.21MB / 134.21MB                                                                                                                                                                                              10.4s
 => => sha256:7bda177df30edadfddb14fc7938185363d1a975fa06f625dfd41bba520bae1ee 54.91kB / 54.91kB                                                                                                                                                                                                 5.9s
 => => extracting sha256:332bf309ecf2b86f336452cb3137f9b266c99d9a10612bd124e2f13e0dad9bb3                                                                                                                                                                                                        1.6s
 => => extracting sha256:2e377b89d6767f434b66aef2b55d4397fa1ca4ef205a16f2fc626005be867634                                                                                                                                                                                                        0.0s
 => => extracting sha256:45705fa60e5881f3a69ceb008964dcca0e72d626655d99b6d92e4c0834c7131b                                                                                                                                                                                                        0.0s
 => => extracting sha256:139b55c8532d30c022b37d343fee5e3b33a0e8e51e903d285b6624f144025cdd                                                                                                                                                                                                        0.0s
 => => extracting sha256:4f4fb700ef54461cfa02571ae0db9a0dc1e0cdb5577484a6d75e68dc38e8acc1                                                                                                                                                                                                        0.0s
 => => extracting sha256:450b7f4750e03e0b16c111f939835cb461fdd66a163773c1c68a5d7cfee2268f                                                                                                                                                                                                        1.3s
 => => extracting sha256:f38f7076143cf0fdb81f64c889e46d42d2a09e9f16c468f28101ed517c1ee156                                                                                                                                                                                                        0.7s
 => => extracting sha256:7bda177df30edadfddb14fc7938185363d1a975fa06f625dfd41bba520bae1ee                                                                                                                                                                                                        0.0s
 => [stage-1 1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd                                                                                                                                                  12.6s
 => => resolve docker.io/library/eclipse-temurin:21-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd                                                                                                                                                           0.0s
 => => sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd 2.68kB / 2.68kB                                                                                                                                                                                                   0.0s
 => => sha256:62fa775039897e4420368514ba6c167741f6d45a0de9ff9125bee57e5aca8b75 1.94kB / 1.94kB                                                                                                                                                                                                   0.0s
 => => sha256:360e75d7612b35a3a65d4b7f2d5ecd735621389fa5e41dd7f55cb4c27878dc1c 3.98kB / 3.98kB                                                                                                                                                                                                   0.0s
 => => sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870 3.64MB / 3.64MB                                                                                                                                                                                                   6.5s
 => => extracting sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870                                                                                                                                                                                                        0.1s
 => => sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750 16.18MB / 16.18MB                                                                                                                                                                                                 8.5s
 => => extracting sha256:f6cd406c8d97cafcb893e824126c17fa19907b2bbc8d759931089e1be1e75750                                                                                                                                                                                                        0.5s
 => => sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a 53.06MB / 53.06MB                                                                                                                                                                                                11.6s
 => => sha256:e6744199aa66ab985e37e72924f1568a6751afa2c508c42a1b3b945f3a8850a7 126B / 126B                                                                                                                                                                                                      10.2s
 => => sha256:cda86626eeb372589c3378d030f4522ba1b0c78ec58b1db87960fa4e5fcd3e34 2.28kB / 2.28kB                                                                                                                                                                                                  10.5s
 => => extracting sha256:74f6a226ed936757680facf9b217f62a2af16b663a69df8e4b3ece925e27ed2a                                                                                                                                                                                                        0.8s
 => => extracting sha256:e6744199aa66ab985e37e72924f1568a6751afa2c508c42a1b3b945f3a8850a7                                                                                                                                                                                                        0.0s
 => => extracting sha256:cda86626eeb372589c3378d030f4522ba1b0c78ec58b1db87960fa4e5fcd3e34                                                                                                                                                                                                        0.0s
 => [internal] load build context                                                                                                                                                                                                                                                                0.0s
 => => transferring context: 46.62kB                                                                                                                                                                                                                                                             0.0s
 => [stage-1 2/4] RUN addgroup -S spring && adduser -S spring -G spring                                                                                                                                                                                                                          0.5s
 => [stage-1 3/4] WORKDIR /app                                                                                                                                                                                                                                                                   0.1s
 => [builder 2/7] WORKDIR /workspace                                                                                                                                                                                                                                                             0.1s
 => [builder 3/7] COPY --chown=gradle:gradle gradle gradle                                                                                                                                                                                                                                       0.0s
 => [builder 4/7] COPY --chown=gradle:gradle build.gradle .                                                                                                                                                                                                                                      0.0s
 => [builder 5/7] COPY --chown=gradle:gradle settings.gradle .                                                                                                                                                                                                                                   0.0s
 => [builder 6/7] COPY --chown=gradle:gradle src src                                                                                                                                                                                                                                             0.0s
 => [builder 7/7] RUN gradle clean build --no-daemon                                                                                                                                                                                                                                            27.7s
 => [stage-1 4/4] COPY --from=builder /workspace/build/libs/*.jar app.jar                                                                                                                                                                                                                        0.0s
 => exporting to image                                                                                                                                                                                                                                                                           0.1s
 => => exporting layers                                                                                                                                                                                                                                                                          0.0s
 => => writing image sha256:24f0306e7864cf4860cfc1030ea405a59e0fe49b7a53dd54fbbff291f3f3acb1                                                                                                                                                                                                     0.0s
 => => naming to docker.io/library/buildkit-spring:latest                                                                                                                                                                                                                                        0.0s

real    0m44.440s
user    0m0.224s
sys     0m0.222s
```



```bash
$ hyperfine \                                                                
  --warmup 1 \
  'docker build -t spring-legacy:legacy .' \
  'docker buildx build --platform linux/amd64 -t buildkit-spring:latest .'
Benchmark 1: docker build -t spring-legacy:legacy .
  Time (mean ± σ):     461.5 ms ±  17.9 ms    [User: 46.3 ms, System: 74.3 ms]
  Range (min … max):   438.2 ms … 495.2 ms    10 runs
 
Benchmark 2: docker buildx build --platform linux/amd64 -t buildkit-spring:latest .
  Time (mean ± σ):     439.6 ms ±  10.2 ms    [User: 44.4 ms, System: 62.8 ms]
  Range (min … max):   426.1 ms … 462.2 ms    10 runs
 
Summary
  'docker buildx build --platform linux/amd64 -t buildkit-spring:latest .' ran
    1.05 ± 0.05 times faster than 'docker build -t spring-legacy:legacy .'
```

## Image Layer

```bash

$ docker history buildkit-spring 
IMAGE          CREATED             CREATED BY                                      SIZE      COMMENT
24f0306e7864   About an hour ago   ENTRYPOINT ["sh" "-c" "java $JAVA_OPTS -jar …   0B        buildkit.dockerfile.v0
<missing>      About an hour ago   EXPOSE map[8080/tcp:{}]                         0B        buildkit.dockerfile.v0
<missing>      About an hour ago   USER spring:spring                              0B        buildkit.dockerfile.v0
<missing>      About an hour ago   COPY /workspace/build/libs/*.jar app.jar # b…   21MB      buildkit.dockerfile.v0
<missing>      2 hours ago         WORKDIR /app                                    0B        buildkit.dockerfile.v0
<missing>      2 hours ago         RUN /bin/sh -c addgroup -S spring && adduser…   3.05kB    buildkit.dockerfile.v0
<missing>      8 weeks ago         ENTRYPOINT ["/__cacert_entrypoint.sh"]          0B        buildkit.dockerfile.v0
<missing>      8 weeks ago         COPY --chmod=755 entrypoint.sh /__cacert_ent…   5.31kB    buildkit.dockerfile.v0
<missing>      8 weeks ago         RUN /bin/sh -c set -eux;     echo "Verifying…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago         RUN /bin/sh -c set -eux;     ARCH="$(apk --p…   164MB     buildkit.dockerfile.v0
<missing>      8 weeks ago         ENV JAVA_VERSION=jdk-21.0.7+6                   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago         RUN /bin/sh -c set -eux;     apk add --no-ca…   33MB      buildkit.dockerfile.v0
<missing>      8 weeks ago         ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_AL…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago         ENV PATH=/opt/java/openjdk/bin:/usr/local/sb…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago         ENV JAVA_HOME=/opt/java/openjdk                 0B        buildkit.dockerfile.v0
<missing>      4 months ago        CMD ["/bin/sh"]                                 0B        buildkit.dockerfile.v0
<missing>      4 months ago        ADD alpine-minirootfs-3.21.3-x86_64.tar.gz /…   7.83MB    buildkit.dockerfile.v0
```

```bash
$ docker history spring-legacy:legacy
IMAGE          CREATED        CREATED BY                                      SIZE      COMMENT
24f0306e7864   2 hours ago    ENTRYPOINT ["sh" "-c" "java $JAVA_OPTS -jar …   0B        buildkit.dockerfile.v0
<missing>      2 hours ago    EXPOSE map[8080/tcp:{}]                         0B        buildkit.dockerfile.v0
<missing>      2 hours ago    USER spring:spring                              0B        buildkit.dockerfile.v0
<missing>      2 hours ago    COPY /workspace/build/libs/*.jar app.jar # b…   21MB      buildkit.dockerfile.v0
<missing>      2 hours ago    WORKDIR /app                                    0B        buildkit.dockerfile.v0
<missing>      2 hours ago    RUN /bin/sh -c addgroup -S spring && adduser…   3.05kB    buildkit.dockerfile.v0
<missing>      8 weeks ago    ENTRYPOINT ["/__cacert_entrypoint.sh"]          0B        buildkit.dockerfile.v0
<missing>      8 weeks ago    COPY --chmod=755 entrypoint.sh /__cacert_ent…   5.31kB    buildkit.dockerfile.v0
<missing>      8 weeks ago    RUN /bin/sh -c set -eux;     echo "Verifying…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago    RUN /bin/sh -c set -eux;     ARCH="$(apk --p…   164MB     buildkit.dockerfile.v0
<missing>      8 weeks ago    ENV JAVA_VERSION=jdk-21.0.7+6                   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago    RUN /bin/sh -c set -eux;     apk add --no-ca…   33MB      buildkit.dockerfile.v0
<missing>      8 weeks ago    ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_AL…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago    ENV PATH=/opt/java/openjdk/bin:/usr/local/sb…   0B        buildkit.dockerfile.v0
<missing>      8 weeks ago    ENV JAVA_HOME=/opt/java/openjdk                 0B        buildkit.dockerfile.v0
<missing>      4 months ago   CMD ["/bin/sh"]                                 0B        buildkit.dockerfile.v0
<missing>      4 months ago   ADD alpine-minirootfs-3.21.3-x86_64.tar.gz /…   7.83MB    buildkit.dockerfile.v0
```