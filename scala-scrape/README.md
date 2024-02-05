# why scala-scrape

I have a friend who wanted me to scrape some of her favourite web novels so she could read them without going online. Sometimes the website or links disappear as the internet changes so we also devised a way to scrape them from the archives.

# prereqs

You'll need all the bits and bobs to run scala

- `coursier 2.1.8`
- `scala 3.3.1`
- `scala-cli 1.1.1`

# how to invoke

honestly I wrote this weeks before actually throwing it all into github so I am scraping my brain here :D

1. package using `scala-cli`
2. invoke: `./JsoupScraper <url-to-scrape> <output-dir>

# some times browsers do weird things depending on where the files are stored

```
sed -i '1s/^/<meta charset="utf-8">\n/' *.html
```


