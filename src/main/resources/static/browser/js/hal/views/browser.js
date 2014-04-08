HAL.Views.Browser = Backbone.View.extend({
  initialize: function(opts) {
    var self = this;
    this.vent = opts.vent;
    this.entryPoint = opts.entryPoint;
    this.explorerView = new HAL.Views.Explorer({ vent: this.vent });
    this.inspectorView = new HAL.Views.Inspector({ vent: this.vent });
  },

  className: 'hal-browser row-fluid',

  render: function() {
    this.$el.empty();

    this.inspectorView.render();
    this.explorerView.render();

    this.$el.html(this.explorerView.el);
    this.$el.append(this.inspectorView.el);

    var entryPoint = this.entryPoint;

    $("#entryPointLink").click(function(event) {
      event.preventDefault();
      window.location.hash = entryPoint;
    });
    return this;
  }
});
