HAL.Views.ResponseBody = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
  },

  className: 'response-headers',

  render: function(e) {
    this.$el.html(this.template({
      body: this._bodyAsStringFromEvent(e)
    }));
  },

  template: _.template($('#response-body-template').html()),

  _bodyAsStringFromEvent: function(e) {
    var output = 'n/a';
    if(e.resource !== null) {
      output = JSON.stringify(e.resource, null, HAL.jsonIndent);
    } else {
      // The Ajax request "failed", but there may still be an
      // interesting response body (possibly JSON) to show.
      var content_type = e.jqxhr.getResponseHeader('content-type');
      var responseText = e.jqxhr.responseText;
      if(content_type == null || content_type.indexOf('text/') == 0) {
        output = responseText;
      } else if(content_type.indexOf('json') != -1) {
        // Looks like json... try to parse it.
        try {
          var obj = JSON.parse(responseText);
          output = JSON.stringify(obj, null, HAL.jsonIndent);
        } catch (err) {
          // JSON parse failed. Just show the raw text.
          output = responseText;
        }
      }
    }
    return output
  }
});
