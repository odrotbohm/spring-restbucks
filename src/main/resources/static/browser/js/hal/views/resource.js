HAL.Views.Resource = Backbone.View.extend({
  initialize: function(opts) {
    var self = this;

    this.vent = opts.vent;

    this.vent.bind('response', function(e) {
      self.render(new HAL.Models.Resource(e.resource));
    });

    this.vent.bind('fail-response', function(e) {
      self.vent.trigger('response', { resource: null, jqxhr: e.jqxhr });
    });
  },

  className: 'resource',

  render: function(resource) {
    var linksView = new HAL.Views.Links({ vent: this.vent }),
        propertiesView = new HAL.Views.Properties({ vent: this.vent }),
        embeddedResourcesView 

    propertiesView.render(resource.toJSON());
    linksView.render(resource.links);

    this.$el.empty();
    this.$el.append(propertiesView.el);
    this.$el.append(linksView.el);

    if (resource.embeddedResources) {
      embeddedResourcesView = new HAL.Views.EmbeddedResources({ vent: this.vent });
      embeddedResourcesView.render(resource.embeddedResources);
      this.$el.append(embeddedResourcesView.el);
    }

    return this;
  }
});
