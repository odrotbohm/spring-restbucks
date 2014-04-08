HAL.Views.Response = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;

    this.headersView = new HAL.Views.ResponseHeaders({ vent: this.vent });
    this.bodyView = new HAL.Views.ResponseBody({ vent: this.vent });

    _.bindAll(this, 'render');

    this.vent.bind('response', this.render);
  },

  className: 'response',

  render: function(e) {
    this.$el.html();

    this.headersView.render(e);
    this.bodyView.render(e);

    this.$el.append(this.headersView.el);
    this.$el.append(this.bodyView.el);
  }
});
