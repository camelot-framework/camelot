/*global angular, d3*/
angular.module('camelotDraggable', []).directive('dragContainer',function () {
    "use strict";
    function Container($element) {
        this.el = $element;
        this.placeholder = $('<div class="widget_placeholder"></div>');
    }
    Container.prototype.getColumnOver = function(x) {
        var columns = this.el.find('.widget_col');
        return columns.filter(function(index) {
            var position = this.getBoundingClientRect(),
                xmin = window.pageXOffset + position.left,
                xmax = window.pageXOffset + position.left + position.width;
            return (xmin <= x || index === 0) && (x <= xmax || index === columns.length - 1)
        });
    };
    Container.prototype.putPlaceholder = function(x, y) {
        var column = this.getColumnOver(x),
            widgets = column.find('[draggable]:not(.widget__dragged)'),
            range = widgets.toArray().reduce(function(range, widget) {
                var position = widget.getBoundingClientRect();
                range[0] = Math.min(range[0], window.pageYOffset + position.top);
                range[1] = Math.max(range[0], window.pageYOffset + position.top + position.height);
                return range;
            }, [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY]);
        if(y < range[0]) {
            this.placeholder.prependTo(column);
        }
        else if(y > range[1]) {
            this.placeholder.appendTo(column);
        }
        else {
            this.placeholder.insertAfter(
                widgets.filter(function() {
                    var position = this.getBoundingClientRect(),
                        ymin = window.pageYOffset + position.top,
                        ymax = window.pageYOffset + position.top + position.height;
                    return ymin <= y && y <= ymax;
                })
            );
        }
    };
    return {
        controller: ['$scope', '$element', function ($scope, $element) {
            function draggableParent(el) {
                while (el && !el.hasAttribute('draggable')) {
                    el = el.parentNode;
                    if (!el.classList) {
                        return null;
                    }
                }
                return el;
            }
            function initDrag(element) {
                var node = element.node(),
                    position = node.getBoundingClientRect();
                element.style({
                        width: element.style('width'),
                        left: window.pageXOffset + position.left + 'px',
                        top: window.pageYOffset + position.top + 'px'
                    })
                    .classed('widget__dragged', true);
                dragStarted = true;
            }
            var container = new Container($element),
                drag = d3.behavior.drag()
                    .on('dragstart.draggable', function onDragStart() {
                        dragStarted = false;
                        dragPrevented = d3.event.sourceEvent.which !== 1;
                    })
                    .on('drag.draggable', function onDragWidget() {
                        var el = d3.select(draggableParent(this));
                        if (dragPrevented) {
                            return;
                        }
                        if (!dragStarted) {
                            initDrag(el);
                        }
                        this.style.display = "none";
                        var left = d3.event.sourceEvent.clientX,
                            top = d3.event.sourceEvent.clientY;
                        container.putPlaceholder(left, top);
                        this.style.display = "block";
                        el.style({
                            left: left + 'px',
                            top: top + 'px'
                        });
                    })
                    .on('dragend.draggable', function onDragEnd() {
                        var node = draggableParent(this);
                        if (dragStarted) {
                            $(node).insertBefore(container.placeholder);
                            container.placeholder.remove();
                            d3.select(node).classed('widget__dragged', false)
                                .style({
                                    width: null,
                                    left: null,
                                    top: null
                                });
                            var widgets = [];
                            d3.select($element[0]).selectAll('.widget_col').each(function () {
                               widgets.push(d3.select(this).selectAll('[draggable]').data())
                            });
                            $scope.$emit('widgetsReorder', widgets);
                        }
                    }),
                dragPrevented = false,
                dragStarted = false;
            this.draggable = function (element) {
                d3.select(element).call(drag);
            };
        }]
    };
}).directive('dragger', function () {
    "use strict";
    return {
        require: '^dragContainer',
        link: function (scope, elm, attrs, DragCtrl) {
            DragCtrl.draggable(elm[0]);
        }
    };
});
