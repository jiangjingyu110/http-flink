### github使用问题
* 提交到github提示“Failed to connect to github.com port 443: Timed out”
  ```shell
  #需要输入如下命令
  git config --global --unset http.proxy
  git config --global --unset https.proxy
  ```

* 打开github慢，需要在系统Hosts文件里面加入如下映射

  ```shell
  192.0.66.2         github.blog
  140.82.112.4       github.com
  140.82.112.18      github.community
  185.199.108.154    github.githubassets.com
  151.101.65.194     github.global.ssl.fastly.net
  185.199.110.153    github.io
  185.199.108.133    github.map.fastly.net
  ```

  