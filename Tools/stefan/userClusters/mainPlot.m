clear ; close all; clc
warning ("off", "Octave:broadcast");

%load('user_data.mat')
load('idx_min.mat')
K_max = size(J_min_vector, 1)

% X: original user data
% idx_min_matrix: m x K_max size matrix with the indices of the users in X that minimize the cost function J
% J_min_vector: K_max size vector with the minimum cost for the index K

for K = 1:K_max 
	plotData(X, idx_min_matrix(:,K), K)
end
