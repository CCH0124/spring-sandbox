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