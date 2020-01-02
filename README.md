# proxywarrior
Forward/Reverse Proxy that can run as standalone proxy server, war application or library included in another java application

[![Build Status](https://travis-ci.org/stasha/proxywarrior.svg?branch=master)](https://travis-ci.org/stasha/proxywarrior)
[![CircleCI](https://circleci.com/gh/stasha/proxywarrior.svg?style=svg)](https://circleci.com/gh/stasha/proxywarrior)
[![Coverage Status](https://coveralls.io/repos/github/stasha/proxywarrior/badge.svg?branch=master)](https://coveralls.io/github/stasha/proxywarrior?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/5be5f846f1f5fca2c466/maintainability)](https://codeclimate.com/github/stasha/proxywarrior/maintainability)

## YAML Configuration
```yaml 
---
properties:
  host: http://localhost
  attrkey: Attribute-Header # attrval is in TestModel

# global request settings
targetUri: _[host]_:9998/proxy
model: info.stasha.proxywarrior.config.TestModel
removeHeaders: ".*"

log:
  content: true
  matchedUrl: true
  headers: true
  
clientConfig:
  handleRedirects: true

headers:
  origin: https://www.testorigin.com
  xtra: _[xtra]_
  _[attrkey]_: _[attrval]_
  +add-if-not-present: add-if-not-present-original
  =keep-eisting: keep-existing-original
  ~delete-existing: deleteheader

requests:
    #------------------------------------------------------
    # Requests should be always ordered from most specific
    # to most general
    #------------------------------------------------------

    # generic test url or url containing /proxy
    # are not proxied to prevent infinite "proxy" loop
  - url: ".*?(__generic__|/proxy).*"
    autoProxy: false
  
    # url for testing file responses
  - url: /fileresponse
    removeHeaders: "null"
    headers: 
        +add-if-not-present: add-if-not-present
        =keep-existing: 
        ~delete-existing: 
    responses:
      - file: classpath:/file.txt
        
    # url for testing text response
  - url: /textresponse
    responses:
      - text: my text from yaml
  
  - url: /methodtextresponse
    removeHeaders: "null"
    responses:
        # response will be matched only if 
        # 1. request method is GET
        # 2. if response contains header (x-get-header)
        # 
        # it will return specified text instead of original response
        #
        # Note that this GET response must be specified before later,
        # GET response because it has more matchers (method, responseHeader)
        # later response has only method matcher.
        # If order was switched from less specific to more specific, more 
        # specific response would never be hit.
      - method: GET
        responseHeader: x-get-header
        text: my get text with header response
        
        # response will be matched only if 
        # 1. request method is GET
        # 
        # it will return specified text instead of original response
      - method: GET
        text: my get text response
        
        # response will be matched only if 
        # 1. request method is POST
        # 
        # it will return specified text instead of original response
      - method: POST
        text: my post text response
        
        # response will be matched only if 
        # 1. request method is PUT
        # 
        # it will return specified text instead of original response
      - method: PUT
        text: my put text response
        
        # response will be matched only if
        # 1. request method is PATCH
        # 2. request contains specified header with specified value
        # 3. response contains specified header with specified value
      - method: DELETE
        requestHeader: "x-delete-request\\s*:\\s*true"
        responseHeader: "x-delete-response\\s*:\\s*true"
        text: my delete text response
        
        # response will be matched only if
        # 1. request method is HEAD or OPTIONS
        # 2. request contains specific request header
        #
        # it will return specified text instead of original response
      - method: "HEAD|OPTIONS"
        requestHeader: "x-multy-method"
        headers:
            x-head-options-header: true
        text: my head|options text response
            
        # all responses that did not match previous response matchers
        # will fall to this default response
        #
        # specified text will be returned instead of original response
        # additional header (x-default-header: true) will be added to response
      - text: my default text response
        headers:
            x-default-header: true
    
    # request configuration that will match
    # any request url is always last
  - url: "*"
  ```
  
