HAL-browser
===========
An API browser for the hal+json media type

Example Usage
=============
Here is an example of a hal+json API using the browser:

[http://haltalk.herokuapp.com/explorer/browser.html](http://haltalk.herokuapp.com/explorer/browser.html)

About HAL
========
HAL is a format based on json that establishes conventions for
representing links. For example:

```javascript
{
    "_links": {
        "self": { "href": "/orders" },
        "next": { "href": "/orders?page=2" }
    }
}
```

More detail about HAL can be found at
[http://stateless.co/hal_specification.html](http://stateless.co/hal_specification.html).

Instructions
============
All you should need to do is copy the files into your webroot.
It is OK to put it in a subdirectory; it does not need to be in the root.

All the JS and CSS dependencies come included in the vendor directory.


TODO
===========
* Make Location and Content-Location headers clickable links
* Provide feedback to user when there are issues with response (missing
self link, wrong media type identifier)
* Give 'self' and 'curies' links special treatment
