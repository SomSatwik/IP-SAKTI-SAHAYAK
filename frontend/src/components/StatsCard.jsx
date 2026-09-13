export default function StatsCard({ title, value, description, icon: Icon, trend }) {
  return (
    <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <h3 className="text-2xl font-bold text-slate-900 mt-1">{value}</h3>
        </div>
        <div className="p-2 bg-indigo-50 rounded-lg">
          <Icon className="w-5 h-5 text-indigo-600" />
        </div>
      </div>
      {(description || trend) && (
        <div className="mt-4 flex items-center text-sm">
          {trend && (
            <span className={`font-medium mr-2 ${trend.isPositive ? 'text-emerald-600' : 'text-red-600'}`}>
              {trend.value}
            </span>
          )}
          <span className="text-slate-500">{description}</span>
        </div>
      )}
    </div>
  );
}
