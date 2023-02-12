### github使用问题
* 提交到github提示“Failed to connect to github.com port 443: Timed out”
  ```shell
  #需要输入如下命令
  git config --global --unset http.proxy
  git config --global --unset https.proxy
  ```