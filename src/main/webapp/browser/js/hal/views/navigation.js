HAL.Views.Navigation = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
    this.locationBar = new HAL.Views.LocationBar({ vent: this.vent });
    this.requestHeadersView = new HAL.Views.RequestHeaders({ vent: this.vent });
  },

  className: 'navigation',

  render: function() {
    this.$el.empty();

    this.locationBar.render();
    this.requestHeadersView.render();

    this.$el.append(this.locationBar.el);
    this.$el.append(this.requestHeadersView.el);
  }
});
