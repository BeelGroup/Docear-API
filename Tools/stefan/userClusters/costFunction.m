function [J] = costFunction(X_norm, centroids, idx)

J = 0;

for class = 1:size(centroids, 1)
	x = X_norm(find(idx==class), :);
	J = J + sum(sqrt(sum( (x.-centroids(class, :)) .^2, 2)));
end

