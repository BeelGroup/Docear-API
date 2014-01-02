function [J] = costFunction(X_norm, centroids, idx)

J = 0;
for class = 1:size(centroids, 1)
	x = X_norm(find(idx==class), :);
	J = J + sum(sum((x.-centroids(class, :)).^2)) / size(x,1);
end

