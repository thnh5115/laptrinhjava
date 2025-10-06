'use client';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LineChart, Line, AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const AreaChartComponent = AreaChart as any;
const LineChartComponent = LineChart as any;
const BarChartComponent = BarChart as any;
const XAxisComponent = XAxis as any;
const YAxisComponent = YAxis as any;
const CartesianGridComponent = CartesianGrid as any;
const TooltipComponent = Tooltip as any;
const LineComponent = Line as any;
const AreaComponent = Area as any;
const BarComponent = Bar as any;
const ResponsiveContainerComponent = ResponsiveContainer as any;

interface ChartData {
  name: string;
  value: number;
  value2?: number;
}

interface DashboardChartProps {
  title: string;
  description?: string;
  data: ChartData[];
  type?: 'line' | 'area' | 'bar';
  dataKey?: string;
  secondaryDataKey?: string;
  color?: string;
  secondaryColor?: string;
}

export function DashboardChart({ 
  title, 
  description, 
  data, 
  type = 'line',
  dataKey = 'value',
  secondaryDataKey,
  color = '#10B981',
  secondaryColor = '#14B8A6'
}: DashboardChartProps) {
  const renderChart = () => {
    switch (type) {
      case 'area':
        return (
          <AreaChartComponent data={data}>
            <CartesianGridComponent strokeDasharray="3 3" />
            <XAxisComponent dataKey="name" />
            <YAxisComponent />
            <TooltipComponent />
            <AreaComponent type="monotone" dataKey={dataKey} stroke={color} fill={color} fillOpacity={0.3} />
            {secondaryDataKey && (
              <AreaComponent type="monotone" dataKey={secondaryDataKey} stroke={secondaryColor} fill={secondaryColor} fillOpacity={0.3} />
            )}
          </AreaChartComponent>
        );
      case 'bar':
        return (
          <BarChartComponent data={data}>
            <CartesianGridComponent strokeDasharray="3 3" />
            <XAxisComponent dataKey="name" />
            <YAxisComponent />
            <TooltipComponent />
            <BarComponent dataKey={dataKey} fill={color} />
            {secondaryDataKey && (
              <BarComponent dataKey={secondaryDataKey} fill={secondaryColor} />
            )}
          </BarChartComponent>
        );
      default:
        return (
          <LineChartComponent data={data}>
            <CartesianGridComponent strokeDasharray="3 3" />
            <XAxisComponent dataKey="name" />
            <YAxisComponent />
            <TooltipComponent />
            <LineComponent type="monotone" dataKey={dataKey} stroke={color} strokeWidth={2} />
            {secondaryDataKey && (
              <LineComponent type="monotone" dataKey={secondaryDataKey} stroke={secondaryColor} strokeWidth={2} />
            )}
          </LineChartComponent>
        );
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent>
        <div className="h-[300px]">
          <ResponsiveContainerComponent width="100%" height="100%">
            {renderChart()}
          </ResponsiveContainerComponent>
        </div>
      </CardContent>
    </Card>
  );
}