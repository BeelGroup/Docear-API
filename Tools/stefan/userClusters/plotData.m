function plotData(X, y, K)
%PLOTDATA Plots the data points X and y into a new figure 
%   PLOTDATA(x,y) plots the data points with + for the positive examples
%   and o for the negative examples. X is assumed to be a Mx2 matrix.

% Create New Figure
figure; hold on;

plotColor = 'rgbkmcyw';

for i = 1:K
	idx = find(y == i);
	
	x = X(idx, :);
	plot(x(:,1), x(:,2), 'ko', 'MarkerFaceColor', plotColor(i), 'MarkerEdgeColor', plotColor(i), 'MarkerSize', 4);

	[X_norm, mu, sigma] = featureNormalize(x);
	plot(mu(1), mu(2), 'k+', 'MarkerEdgeColor', plotColor(i),  'MarkerSize', 10);

end


% =========================================================================



hold off;

end
