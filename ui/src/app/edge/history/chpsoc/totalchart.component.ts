import { Component, OnInit, OnChanges, Input } from "@angular/core";
import { DefaultTypes } from 'src/app/shared/service/defaulttypes';
import { Service, Edge, ChannelAddress, Utils } from 'src/app/shared/shared';
import { ActivatedRoute } from '@angular/router';
import { Dataset, EMPTY_DATASET, ChartOptions, DEFAULT_TIME_CHART_OPTIONS, TooltipItem, Data } from '../shared';
import { QueryHistoricTimeseriesDataResponse } from 'src/app/shared/jsonrpc/response/queryHistoricTimeseriesDataResponse';
import { formatNumber } from '@angular/common';
import { AbstractHistoryChart } from '../abstracthistorychart';

@Component({
    selector: 'chpsocTotalChart',
    templateUrl: '../abstracthistorychart.html'
})
export class ChpSocTotalChartComponent extends AbstractHistoryChart implements OnInit, OnChanges {

    @Input() private period: DefaultTypes.HistoryPeriod;
    @Input() private isOnlyChart: boolean;

    ngOnChanges() {
        this.updateChart();
    };

    constructor(
        protected service: Service,
        private route: ActivatedRoute,
    ) {
        super(service);
    }

    protected updateChart() {
        this.colors = [];
        this.loading = true;
        this.queryHistoricTimeseriesData(this.period.from, this.period.to).then(response => {
            let result = (response as QueryHistoricTimeseriesDataResponse).result;

            // convert labels
            let labels: Date[] = [];
            for (let timestamp of result.timestamps) {
                labels.push(new Date(timestamp));
            }
            this.labels = labels;

            // show Channel-ID if there is more than one Channel
            let showChannelId = Object.keys(result.data).length > 1 ? true : false;

            // convert datasets
            let datasets = [];
            Object.keys(result.data).forEach((channel, index) => {
                let address = ChannelAddress.fromString(channel);
                let data = result.data[channel].map(value => {
                    if (value == null) {
                        return null
                    } else {
                        return Math.floor(parseInt(value)); //  Rounding up the mean values to integer value 
                    }
                });

                switch (index % 2) {
                    case 0:
                        datasets.push({
                            label: "Ausgang" + (showChannelId ? ' (' + address.channelId + ')' : ''),
                            data: data
                        });
                        this.colors.push({
                            backgroundColor: 'rgba(0,191,255,0.05)',
                            borderColor: 'rgba(0,191,255,1)',
                        })
                        break;
                    case 1:
                        datasets.push({
                            label: "Ausgang" + (showChannelId ? ' (' + address.channelId + ')' : ''),
                            data: data
                        });
                        this.colors.push({
                            backgroundColor: 'rgba(0,0,139,0.05)',
                            borderColor: 'rgba(0,0,139,1)',
                        })
                        break;
                }
            })
            this.datasets = datasets;
            this.loading = false;

        }).catch(reason => {
            console.error(reason); // TODO error message
            this.initializeChart();
            return;
        });
    }

    ngOnInit() {
        this.service.setCurrentComponent('', this.route);
        this.setLabel();
    }

    protected getChannelAddresses(edge: Edge): Promise<ChannelAddress[]> {
        return new Promise((resolve, reject) => {
            this.service.getConfig().then(config => {
                let channeladdresses = [];
                // find all chpsoc components
                for (let componentId of config.getComponentsByFactory("Controller.CHP.SoC")) {
                    channeladdresses.push(ChannelAddress.fromString(componentId.properties.outputChannelAddress));
                }
                resolve(channeladdresses);
            }).catch(reason => reject(reason));
        });
    }

    protected setLabel() {
        let options = <ChartOptions>Utils.deepCopy(DEFAULT_TIME_CHART_OPTIONS);
        options.scales.yAxes[0].scaleLabel.labelString = "On/Off";
        options.tooltips.callbacks.label = function (tooltipItem: TooltipItem, data: Data) {
            let label = data.datasets[tooltipItem.datasetIndex].label;
            let value = tooltipItem.yLabel;
            let parsedIntValue = Math.floor(parseInt(formatNumber(value, 'de', '1.0-2')))
            if (parsedIntValue == 1) {
                return label + ": " + "ON";
            } else {
                return label + ":" + "OFF"
            }
        }
        this.options = options;
    }

    public getChartHeight(): number {
        if (this.isOnlyChart == true) {
            return window.innerHeight / 1.2;
        } else if (this.isOnlyChart == false) {
            return window.innerHeight / 21 * 9;
        }
    }
}