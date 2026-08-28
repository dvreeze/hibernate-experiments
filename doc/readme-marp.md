
# Using Marp to create Markdown slide shows

First install Marp as Docker image. See
[Marp installation guide](https://www.devopsschool.com/blog/complete-marp-installation-command-examples-guide/).

```bash
docker pull marpteam/marp-cli

docker run --rm marpteam/marp-cli --version
```

Using Marp, in a rather involved way (in order to avoid access rights issues to the volume directory):

```bash
# In terminal 1, in this project's root directory
cp ./doc/using-hibernate-orm-effectively-slides.md /tmp

# In terminal 2, in directory /tmp
docker run -v "$PWD:/tmp" --rm marpteam/marp-cli /tmp/using-hibernate-orm-effectively-slides.md

# In terminal 1, in this project's root directory
cp /tmp/using-hibernate-orm-effectively-slides.html ./doc
```
