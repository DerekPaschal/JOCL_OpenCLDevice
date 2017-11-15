kernel void Grav(global const float *X, global const float *Y, global const float *Z,
					global float *Xa, global float *Ya, global float *Za, global const float *M,
					local float4 *pblock)
{
	
	int gid = get_global_id(0);
	int lid = get_local_id(0);
	
	int n = get_global_size(0);
	int nt = get_local_size(0);
	int nb = n/nt;
	
	if (gid < n) {
	
		float4 Acc = {0.0f,0.0f,0.0f, 0.0f};
		float4 Pos = {X[gid], Y[gid], Z[gid], 0.0f};
		float4 dist;
		float distMag;
		
		for(int jb = 0; jb < nb; jb++) {
			
			pblock[lid].x = X[jb*nt+lid];
			pblock[lid].y = Y[jb*nt+lid];
			pblock[lid].z = Z[jb*nt+lid];
			pblock[lid].w = M[jb*nt+lid];
			//{X[jb*nt+lid], Y[jb*nt+lid], Y[jb*nt+lid], M[jb*nt+lid]};
			barrier(CLK_LOCAL_MEM_FENCE);
			
			for(int j = 0; j < nt; j++) {
				dist = pblock[j] - Pos;
			
				distMag = (sqrt((dist.x*dist.x)  + (dist.y*dist.y) + (dist.z*dist.z))) + 0.0000001f;
				
				//Gravity
				Acc +=  dist * (dist.w/ ((distMag*distMag)));
			}
		}
		
		Xa[gid] = Acc.x;
		Ya[gid] = Acc.y;
		Za[gid] = Acc.z;
	
	}
}

kernel void Step(global float *X, global float *Y, global float *Z,
					global float *Xv, global float *Yv, global float *Zv,
					global float *Xa, global float *Ya, global float *Za,
					global float *M) 
{
	int gid = get_global_id(0);
	
	if (gid < get_global_size(0)) {
		Xv[gid] += Xa[gid];
		Yv[gid] += Ya[gid];
		Zv[gid] += Za[gid];
	
		X[gid] += Xv[gid];
		Y[gid] += Yv[gid];
		Z[gid] += Zv[gid];
	}
}