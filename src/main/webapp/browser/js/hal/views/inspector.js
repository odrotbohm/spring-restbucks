HAL.Views.Inspector = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;

    _.bindAll(this, 'renderDocumentation');
    _.bindAll(this, 'renderResponse');

    this.vent.bind('show-docs', this.renderDocumentation);
    this.vent.bind('response', this.renderResponse);
  },

  className: 'inspector span6',

  render: function() {
    this.$el.html(this.template());
  },

  renderResponse: function(response) {
    var responseView = new HAL.Views.Response({ vent: this.vent });

    this.render();
    responseView.render(response);

    this.$el.append(responseView.el);
  },

  renderDocumentation: function(e) {
    var docView = new HAL.Views.Documenation({ vent: this.vent });

    this.render();
    docView.render(e.url);

    this.$el.append(docView.el);
  },

  template: function() {
    return '<h1>Inspector</h1>';
  }
});
