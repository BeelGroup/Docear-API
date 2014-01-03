debug_on_warning(1)
clear ; close all; clc
warning ("off", "Octave:broadcast");

fprintf('clustering Docear users using kMeans\n\n');
load('user_data.mat')

% ---------------------------------------
% initialization part
X = data(:, 3);
X = [X, data(:, 6)./data(:, 4)]; % average size

K_max = 6
max_iter=10

% main script
% ---------------------------------------
% ---------------------------------------
% ---------------------------------------

user_ids = data(:,1);
idx_min_matrix = [];
J_min_vector = [];

% normalize the features
[X_norm, mu, sigma] = featureNormalize(X);

% K clusters / max_iters iterations
for K = 1:K_max
	K
	
	J = -1;
	J_min = -1;
	
	idx_min = zeros(size(X,1), 1);
	for i = 1:max_iter
	    % init centroids randomly
	    initial_centroids = kMeansInitCentroids(X_norm, K);
	
	    % Run K-Means
	    [centroids, idx] = runkMeans(X_norm, initial_centroids, 1000000);

		% if centroids were removed, try again
		if size(centroids, 1) < K
			i = i-1;
			continue;
		end
	
	    J = costFunction(X_norm, centroids, idx);
	    if J_min < 0 || J < J_min
	        J_min = J
	        idx_min = idx;
	    end
	end

	idx_min_matrix = [idx_min_matrix, idx_min];
	J_min_vector = [J_min_vector; J_min];
end

save idx_min.mat X idx_min_matrix J_min_vector 
