HAL.Views.ResponseHeaders = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
  },

  className: 'response-headers',

  render: function(e) {
    this.$el.html(this.template({
      status: {
        code: e.jqxhr.status,
        text: e.jqxhr.statusText
      },
      headers: e.jqxhr.getAllResponseHeaders()
    }));
  },

  template: _.template($('#response-headers-template').html())
});
