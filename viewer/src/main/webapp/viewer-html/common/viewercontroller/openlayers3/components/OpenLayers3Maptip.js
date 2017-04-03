/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define ("viewer.viewercontroller.openlayers3.components.OpenLayers3Maptip",{
    extend: "viewer.viewercontroller.openlayers3.OpenLayers3Component",    
    map: null,
    timerId: null,
    delay: 500,
    /**
     * @constructor
     * @param {Object} conf
     * @param {type} map
     * @returns {viewer.viewercontroller.openlayers.components.OpenLayersMaptip}
     */
    constructor: function(conf,map){
        viewer.viewercontroller.openlayers3.components.OpenLayers3Maptip.superclass.constructor.call(this,conf);
        this.map = map;
        //console.log(conf);
        var me  = this;
        this.frameworkObject = new ol.interaction.Pointer({handleMoveEvent:function(evt){console.log('');},
        handleUpEvent:function(evt){console.log('pause');}
        });
        this.map.getFrameworkMap().on('pointermove', function(evt) {
            this.clearTimer();
            this.timerId = window.setTimeout(function(){me.onPause(evt); },this.delay);
      },this);
        
    },

    
    clearTimer: function() {
        if(this.timerId != null) {
            window.clearTimeout(this.timerId);
            this.timerId = null;
        }
    },
    
    onPause : function(evt){
        console.log('pause?');
        var crd = evt.coordinate;
        var pix = evt.pixel;
            
        var options ={
            x:pix[0],
            y:pix[1],
            coord: {
                x:crd[0],
                y:crd[1]
            }
        };
        this.map.fire(viewer.viewercontroller.controller.Event.ON_MAPTIP,options);
    }
    
    
    });