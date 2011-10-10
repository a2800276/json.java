#json.java

Meant to be a simple JSON parsing (and producing) library for Java. It's intended use case is dealing with JSON in the context of `nio` i.e. non-blocking IO where bits and pieces of a payload may arrive and need to be processed. json.java contains a statemachine based parser implementation that allows for this. Example:

	JSON   json = new JSON();
	char [] arr = …

    while (arr = somebitofwhatever) { // bad example*
		json.parse(arr);
		if (json.done()) break;		
	} 

	Object parsed_json = json.obj();
 
It's written in a very simplistic style that some people may not like. For example, I tend to use short variable names, use default (aka package) access and dislike `com.really.long.packagenames`.

\* this is a bad example for non-blocking IO, but I'm using it to keep the example short, imagine a `select` loop that only calls `parse()` when data is available.


## Usage

### Simple parsing

In the simplest form, the parsing and production API follow the Javascript API: `JSON.parse(String)` and `JSON.stringigy(Object)`.

### Resulting Java types of parsed data

* JSON-Objects are converted to `java.util.Map`s
* JSON-Arrays are converted to `java.util.List`s
* Strings, `true`, `false` and `null` are converted to their Java counterparts
* JSON-Number's are converted to `java.math.BigDecimal`s

Thus:

	String json = "{\"a\":[1]}";
	Map    m    = (Map)json.JSON.parse(json);
    
	List<BugDecimal> l = (List<BigDecimal>)m.get("a");
	


### Non-blocking parsing

As stated above, the intended use case is state machine based parsing. (This is the same example as used in the introduction):

	JSON   json = new JSON();
	char [] arr = …

    while (arr = somebitofwhatever) { // bad example*
		json.parse(arr);
		if (json.done()) break;		
	} 

	Object parsed_json = json.obj();
 


### Arbitrary Object deserialization

In case you have a custom class you wish to deserialize from JSON, I have good news and bad news. The bad news: there is no comprehensive system of generics, annotations and dependency injection semantics to configure deserializing JSON into arbitrary class structures. The good news: there is no comprehensive system of generics, annotations and dependency injection semantics to configure deserializing JSON into arbitrary class structures, instead you can use `json.Lexer` to tokenize a stream of JSON and provide callbacks to implement custom deserialization.

This is, in fact how the default parser is implemented. In case you would like to write a custom callback, have a look at the generic `JSON.LexerCB` contained in `src/json/JSON` for inspiration.

### Creating JSON

The facilities for creating JSON are fairly primitive and aren't at the focus of my attention. Currently the same sort of stuff™ the library produces when parsing (i.e. Lists, Maps, primary data types and Strings) can be converted to JSON automatically. Also Arrays may be used instead of Lists. Apart from that, you're currently on your own.


## Miscellanea

Parsing supports a superset of JSON as described [here][json]. The default parser will also handle JSON lacking redundant commas and colons within Objects and Arrays. This:

    { "bla" "blub" "a" [1 2 3]}

is the equivalent of:

	{
		"bla" : "blub",
		"a"   : [1,2,3],
	}

I'll probably add a strict mode at some point.

[json]:http://www.json.org

### TODO

* perhaps I'll add support for a speedy mode that doesn't convert numbers and/or passes Strings as primitive arrays or Buffers …

* additional serialization strategies
	* serialize arbitray classes with `toString()`
	* Beans style serialization (yuck!)
	* treat arbitrary classes as data structs and
      serialize all public fields
    * add support for adding custom handlers for unknown 	  classes

* perhaps honor `toJSON()` methods ...
* proper and consistant error handling
* scavenge comprehensive test vectors for other project
* some rudimentary build system



## License

At this point, I would not yet consider this code "production quality". Once it is, it will be licensed either MIT, BSD or similarly liberal.

In case you'd like to use it at this point anyhow, let me know and I'll accelerate thinking about it.

At this point in time, consider the code:

  ©2011 Tim Becker <tim@kuriositaet.de>