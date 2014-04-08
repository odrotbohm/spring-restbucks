(function() {
  var urlRegex = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;

  function isCurie(string) {
    return string.split(':').length > 1;
  };

  var HAL = {
    Models: {},
    Views: {},
    Http: {},
    currentDocument: {},
    jsonIndent: 2,
    isUrl: function(str) {
      return str.match(urlRegex) || isCurie(str);
    },
    truncateIfUrl: function(str) {
      var replaceRegex = /(http|https):\/\/([^\/]*)\//;
        return str.replace(replaceRegex, '.../');
    },
    buildUrl: function(rel) {
      if (!rel.match(urlRegex) && isCurie(rel) && HAL.currentDocument._links.curies) {
        var parts = rel.split(':');
        var curies = HAL.currentDocument._links.curies;
        for (var i=0; i<curies.length; i++) {
          if (curies[i].name == parts[0]) {
            var tmpl = uritemplate(curies[i].href);
            return tmpl.expand({ rel: parts[1] });
          }
        }
      }
      else if (!rel.match(urlRegex) && isCurie(rel) && HAL.currentDocument._links.curie) {
        // Backward compatibility with <04 version of spec.
        var tmpl = uritemplate(HAL.currentDocument._links.curie.href);
        return tmpl.expand({ rel: rel.split(':')[1] });
      }
      else {
        return rel;
      }
    },
    parseHeaders: function(string) {
      var header_lines = string.split("\n");
      var headers = {};
      _.each(header_lines, function(line) {
        var parts = line.split(':');
        if (parts.length > 1) {
          var name = parts.shift().trim();
          var value = parts.join(':').trim();
          headers[name] = value;
        }
      });
      return headers;
    },
  };

  window.HAL = HAL;
})();
