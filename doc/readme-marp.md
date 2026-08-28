
# Using Marp to create Markdown slide shows

First install Marp as Docker image. See
[Marp installation guide](https://www.devopsschool.com/blog/complete-marp-installation-command-examples-guide/).

```bash
docker pull marpteam/marp-cli

docker run --rm marpteam/marp-cli --version
```

Using Marp, in a rather involved way (in order to avoid access rights issues to the volume directory):

```bash
cp ./doc/using-hibernate-orm-effectively-slides.md /tmp

docker run -v "$PWD:/tmp" --rm marpteam/marp-cli /tmp/using-hibernate-orm-effectively-slides.md

cp /tmp/using-hibernate-orm-effectively-slides.html ./doc
```
