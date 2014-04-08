HAL.Views.Properties = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
    _.bindAll(this, 'render');
  },

  className: 'properties',

  render: function(props) {
    this.$el.html(this.template({ properties: props }));
  },

  template: _.template($('#properties-template').html())
});
