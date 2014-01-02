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
%class_1 = find(y == 1);
%class_2 = find(y == 2);
%class_3 = find(y == 3);
%class_4 = find(y == 4);
%% Plot Examples
%plot(X(class_1, 1), X(class_1, 2), 'ko', 'MarkerFaceColor', 'b', 'MarkerSize', 7);
%plot(X(class_2, 1), X(class_2, 2), 'ko', 'MarkerFaceColor', 'y', 'MarkerSize', 7);
%plot(X(class_3, 1), X(class_3, 2), 'ko', 'MarkerFaceColor', 'r', 'MarkerSize', 7);
%plot(X(class_4, 1), X(class_4, 2), 'ko', 'MarkerFaceColor', 'g', 'MarkerSize', 7);
%
%cx = class_1
%circles(centroids(:,1), centroids(:,2), ones(size(centroids, 1), 1)*1, '+')





% =========================================================================



hold off;

end
