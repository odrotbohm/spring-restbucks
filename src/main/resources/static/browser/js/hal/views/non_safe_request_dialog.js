HAL.Views.NonSafeRequestDialog = Backbone.View.extend({
  initialize: function(opts) {
    this.href = opts.href;
    this.vent = opts.vent;
    this.uriTemplate = uritemplate(this.href);
    _.bindAll(this, 'submitQuery');
  },

  events: {
    'submit form': 'submitQuery'
  },

  className: 'modal fade',

  submitQuery: function(e) {
    e.preventDefault();

    var self = this,
        opts = {
          url: this.$('.url').val(),
          headers: HAL.parseHeaders(this.$('.headers').val()),
          method:  this.$('.method').val(),
          data: this.$('.body').val()
        };

    var request = HAL.client.request(opts);
    request.done(function(response) {
      self.vent.trigger('response', { resource: response, jqxhr: jqxhr });
    }).fail(function(response) {
      self.vent.trigger('fail-response', { jqxhr: jqxhr });
    }).always(function() {
      self.vent.trigger('response-headers', { jqxhr: jqxhr });
      window.location.hash = 'NON-GET:' + opts.url;
    });

    this.$el.modal('hide');
  },

  render: function(opts) {
    var headers = HAL.client.getDefaultHeaders(),
        headersString = '';

    _.each(headers, function(value, name) {
      headersString += name + ': ' + value + '\n';
    });

    this.$el.html(this.template({ href: this.href, user_defined_headers: headersString }));
    this.$el.modal();
    return this;
  },

  template: _.template($('#non-safe-request-template').html())
});
