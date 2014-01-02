clear ; close all; clc
warning ("off", "Octave:broadcast");

fprintf('clustering Docear users using kMeans\n\n');

load('user_data.mat')

user_ids = data(:,1);
X = data(:, 3);
X = [X, data(:, 6)./data(:, 4)]; % average size

% normalize the features
[X_norm, mu, sigma] = featureNormalize(X);

% K clusters / max_iters iterations
K = 6 
max_iters = 10000

J = -1
J_min = -1;

idx_min = zeros(size(X,1), 1);
for i = 1:5
	% init centroids randomly
	initial_centroids = kMeansInitCentroids(X_norm, K);
	
	% Run K-Means
	i
	[centroids, idx] = runkMeans(X_norm, initial_centroids, max_iters);
	
	J = costFunction(X_norm, centroids, idx);
	if J_min < 0 | J < J_min
		J_min = J
		idx_min = idx;
	end
end

J_min
save idx_min.mat idx_min J_min

plotData(X, idx_min, K)

%get all data of class 2
%class_2=X_sav(find(idx==2), :)
